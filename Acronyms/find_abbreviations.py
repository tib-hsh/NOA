import re
import sys
from nltk.tokenize import sent_tokenize, WhitespaceTokenizer
import re
from nltk.stem import WordNetLemmatizer
from nltk import word_tokenize, pos_tag
from pymongo import MongoClient
import configparser

config = configparser.ConfigParser()
config.read('config.ini', encoding='utf-8-sig')

mongoIP = config['DEFAULT']['mongoIP']
mongoPort = int(config['DEFAULT']['mongoPort'])
mongoDB = config['DEFAULT']['mongoDB']
article_collection = config['DEFAULT']['article_collection']
image_collection = config['DEFAULT']['image_collection']

#find sequence of words starting with the acronym letters
def find_best_long_form1(long_form, short_form):
    words = re.split(r'[ -]+',long_form.strip())
    start = 0
    end = 0
    
    words = [w.strip() for w in words if len(w.strip()) > 1]
    if len(words) < 2:
        return None
    
    sindex = len(short_form) - 1
    lindex = len(words) - 1
    
    current_char = short_form[sindex].lower()
    
   

    #find last word starting with current_char
    while lindex > 0 and words[lindex][0].lower() !=  current_char:
        lindex -= 1
    end = lindex + 1
    
    lindex -= 1
    sindex -= 1
    current_char = short_form[sindex].lower()

    
    while sindex >= 0 and lindex >= 0: 
        if words[lindex][0].lower() ==  current_char: 
            sindex -= 1
            current_char = short_form[sindex].lower()
            start = lindex
        lindex -=1
    
    if sindex < 0 and short_form not in words[start:end]:
        return ' '.join(words[start:end])
    
    return None
        

# The following function ist the procedure from Schwartz and Hearst
def find_best_long_form2(long_form, short_form):
    current_char = ''
    sindex = len(short_form) - 1
    lindex = len(long_form) - 1

    
    while sindex >= 0:
        current_char = short_form[sindex].lower()
        if not current_char.isalnum():
            sindex -=1
            continue
        while (lindex  >= 0 and long_form[lindex].lower() != current_char) or (sindex == 0 and lindex > 0 and long_form[lindex-1].isalnum()):
            lindex -= 1
        if lindex < 0:
            return None
        lindex -= 1
        sindex -=1
    if lindex <= 0:
        lindex = 0
    else:
        lindex = long_form.rfind(' ',0,lindex+1) + 1  
    if len(long_form) - lindex <= len(short_form):
        return None
    if short_form.lower() in long_form[lindex:].lower().split():
        return None
    return long_form[lindex:]

def find_best_long_form(long_form, short_form):
    definition =  find_best_long_form1(long_form, short_form)
    if definition == None:
        definition =  find_best_long_form2(long_form, short_form)
    return definition
    
    
def find_acr_definitions(acr, sentences):
    for sent in sentences:
        if acr in sent:
            sf_position = sent.index(acr)
            #short form in brackets immediately behind long form
            if sf_position > 3 and len(sent) > sf_position + 1 and sent[sf_position-1] == '(' and sent[sf_position+1] == ')' :
                start = max([0,sf_position - len(acr) - 5, sf_position - 2 * len(acr) ])
                candidate = ' '.join(sent[start:sf_position-1])
                lf = find_best_long_form(candidate,acr)
                if lf != None:
                    return lf
            #long form in brackets immediately behind short form
            elif len(sent) > sf_position + 3 and sent[sf_position+1] == '(' and sent[sf_position+2][0].lower() == acr[0].lower() and ')' in sent[sf_position + 2:]:
                end = sent.index(')')
                candidate = ' '.join(sent[sf_position+2:end])
                lf = find_best_long_form2(candidate,acr)
                if lf != None:
                    return lf
                
    return None

def save_acr_def(image,acr,definition):
    #print(acr,definition)
    if  definition != None:
        db_images.update(image, {'$addToSet': { 'acronym': (acr, definition)}}, upsert=False, multi=True)
    else:
        db_images.update(image, {'$addToSet': {'acronym': (acr, None)}}, upsert=False, multi=True)

       
        
def find_acronym():
    processed = 0
    for paper in articles:    
        if 'body' not in paper or not isinstance(paper['body'],str):
            continue    
        paper_body = paper['body']
        paper_id = paper['_id']
        
        images = db_images.find({ "sourceID": paper_id })
        
        if images.count() < 1:
            continue
        
        if 'abstract' in paper and isinstance(paper['abstract'],str):
            paper_body  = paper['abstract'] + '\n'  + paper_body
        if 'title' in paper and isinstance(paper['title'],str):
            paper_body  = paper['title'] + '\n'  + paper_body

        sentences = [word_tokenize(s) for s in  sent_tokenize(paper_body)]
        
        paper_acr = {}
        
        #goes through each image caption
        for img in images:
            caption = img['captionBody']
            if caption == None or len(caption) < 5:
                continue
            sentences_caption = [word_tokenize(s) for s in  sent_tokenize(caption)]
            acr_in_caption = []
            for token in tokenizer.tokenize(caption):
                token = token.strip("-.,:;)()\"“”\'[]<>")
                if token.isupper() and 2 < len(token) <= 5:               
                    acr_in_caption.append(token)
            acr_in_caption = list(set(acr_in_caption))

            for acr in acr_in_caption:
                if acr not  in paper_acr:
                    abbrev_def= find_acr_definitions(acr, sentences_caption)
                    if abbrev_def == None:          
                        abbrev_def= find_acr_definitions(acr, sentences)
                    paper_acr[acr] = abbrev_def
                save_acr_def(img,acr,paper_acr[acr]) 
        processed += 1
        if processed % 1000 == 0:
            print(processed, "papers processed")
        sys.stdout.flush()
        
        
        
client = MongoClient(mongoIP, mongoPort)
db = client[mongoDB]
db_images = db[image_Collection]
db_articles = db[article_collection]
articles = db_articles.find({})


wnl = WordNetLemmatizer()
tokenizer = WhitespaceTokenizer()


find_acronym()
