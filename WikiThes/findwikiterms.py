from pymongo import MongoClient
from nltk.corpus import wordnet as wn
from nltk.corpus import stopwords
import argparse

import json
import codecs
import nltk
import re

argpar = argparse.ArgumentParser(description='Find wikipedia terms and store them in Mongo.')
argpar.add_argument('-verbose', action='store_true')
argpar.add_argument("-mongoIP", type=str, required=True)
argpar.add_argument("-mongoPort", type=int, required=True)
argpar.add_argument("-mongoDB", type=str, required=True)
argpar.add_argument("-imageCollection", type=str, required=True)
args = argpar.parse_args()
verbose = args.verbose

vowel = re.compile(r'[aouei]')

client = MongoClient(args.mongoIP, args.mongoPort)
db = client[args.mongoDB]
collection = db[args.imageCollection]

swlist = stopwords.words('english')

ABERRANT_PLURAL_MAP = {
    'appendix': 'appendices',
    'barracks': 'barracks',
    'cactus': 'cacti',
    'child': 'children',
    'criterion': 'criteria',
    'deer': 'deer',
    'echo': 'echoes',
    'elf': 'elves',
    'embargo': 'embargoes',
    'focus': 'foci',
    'fungus': 'fungi',
    'goose': 'geese',
    'hero': 'heroes',
    'hoof': 'hooves',
    'index': 'indices',
    'knife': 'knives',
    'leaf': 'leaves',
    'life': 'lives',
    'man': 'men',
    'maximum':'maxima',
    'minimum':'minima',
    'mouse': 'mice',
    'nucleus': 'nuclei',
    'person': 'people',
    'phenomenon': 'phenomena',
    'potato': 'potatoes',
    'self': 'selves',
    'syllabus': 'syllabi',
    'tomato': 'tomatoes',
    'torpedo': 'torpedoes',
    'veto': 'vetoes',
    'woman': 'women',
    }

VOWELS = set('aeiou')

def pluralize(singular):
    if not singular:
        return ''
    if singular.isupper():
        return ''
    if vowel.search(singular) is None:
        return  ''
    plural = ABERRANT_PLURAL_MAP.get(singular)
    if plural:
        return plural
    root = singular
    try:
        if singular[-1] == 'y' and singular[-2] not in VOWELS:
            root = singular[:-1]
            suffix = 'ies'
        elif singular[-1] == 's':
            if singular[-2] in VOWELS:
                if singular[-3:] == 'ius':
                    root = singular[:-2]
                    suffix = 'i'
                else:
                    root = singular[:-1]
                    suffix = 'ses'
            else:
                suffix = 'es'
        elif singular[-2:] in ('ch', 'sh'):
            suffix = 'es'
        else:
            suffix = 's'
    except IndexError:
        suffix = 's'
    plural = root + suffix
    return plural


grammar = r"""
   NP: {(<CD>)?(<JJ>)*<N(N|P).*>+}
"""


npparser = nltk.RegexpParser(grammar)



def readterms(fname):
    with open(fname) as fin:
        wc = json.loads(fin.read(),encoding="utf-8")
    return dict(zip(map(str.lower,wc.keys()),wc.values()))

    
wikiterms = readterms("wikicats.json")
#wikiterms = readterms("wikicatsrd.json")
#wikiterms = {**wikiterms1, **wikiterms2}

def lookup(p):
    if p in wikiterms:
        return p
    else:
        words = p.split(' ')
        lemma = wn.morphy(words[-1],wn.NOUN)
        if lemma != None and lemma != words[-1]:
            p = ' '.join(words[:-1]+[lemma])
            if p in wikiterms:
                return p
        else:
            plural = pluralize(words[-1])
            if wn.morphy(plural,wn.NOUN) == words[-1]:
                p = ' '.join(words[:-1]+[plural])
                if p in wikiterms:
                    return p  
            elif len(words) > 1: #Try to find plural only for phrases, not for single words (see power, target, delay, influence, inside etc.)
                plural = pluralize(words[-1])
                if wn.morphy(plural,wn.NOUN) == words[-1]:
                    p = ' '.join(words[:-1]+[plural])
                    if p in wikiterms:
                        filtered.append(p)
                    
                    
def findterms_v0(text):
    done = set()
    found = []
    try:
        sentences = nltk.sent_tokenize(text,language='english')
        for sent in sentences:
            tokens = nltk.word_tokenize(sent, language='english')
            tagged_tokens = nltk.pos_tag(tokens)
            tree = npparser.parse(tagged_tokens)
            for node in tree:
                if isinstance(node, nltk.tree.Tree) and  node.label() == 'NP':
                    phrase = ' '.join([word.lower() for word,pos in node.leaves()])
                    if len(phrase) > 2 and phrase not in swlist and phrase not in done:
                        done.add(phrase)
                        w = lookup(phrase)
                        if w != None and w not in found:
                            found.append(w)
                        elif node.leaves()[0][1] == 'JJ' or node.leaves()[0][1] == 'CD':
                            phrase = ' '.join([word.lower() for word,pos in node.leaves()[1:]])
                            if len(phrase) > 2 and phrase not in swlist and phrase not in done:
                                done.add(phrase)
                                w = lookup(phrase)
                                if w != None and w not in found:
                                    found.append(w)                                             
    except:
        pass
    return found
        
        
def findterms(text):
    done = set()
    found = []
    try:
        sentences = nltk.sent_tokenize(text,language='english')
        for sent in sentences:
            tokens = nltk.word_tokenize(sent, language='english')
            tagged_tokens = nltk.pos_tag(tokens)
            tree = npparser.parse(tagged_tokens)
            for node in tree:
                if isinstance(node, nltk.tree.Tree) and  node.label() == 'NP':
                    leaves = node.leaves()
                    while len(leaves) > 0:
                        phrase = ' '.join([word.lower() for word,pos in leaves])
                        if len(phrase) > 2 and phrase not in swlist and phrase not in done:
                            done.add(phrase)
                            w = lookup(phrase)
                            if w != None and w not in found:
                                found.append(w)
                                break
                        noun = leaves[0]
                        if noun[1] != 'JJ' and noun[1]  != 'CD':
                            phrase = noun[0].lower()
                            if len(phrase) > 2 and phrase not in swlist and phrase not in done:
                                done.add(phrase)
                                w = lookup(phrase)
                                if w != None and w not in found:
                                    found.append(w)
                        leaves = leaves[1:]                                          
    except:
        pass
    return found

processed = 0
images = collection.find({})
cache = {}
for f in images:
    processed += 1
    if processed%1000 == 0:
        print(processed, "records processed.")
    caption = f['captionBody']
    findingID = f['findingID']
    if 'context' in f and f['context'] != None and caption != None:
        context = ' '.join(f['context'])
        text = caption+'\n'+context
    elif caption != None:
        text = caption
    elif f['context'] != None:
        text = ' '.join(f['context'])
    else:
        continue
    if text not in cache:
        wikititles = findterms(text)
        if 'acronym' in f:
            acronyms = f['acronym']
            for acr,exp in acronyms:
                if exp != None:
                    wt = lookup(exp.lower())
                    if wt != None and wt not in wikititles:
                        wikititles.append(wt)
        #if 'keywords' in r:
        #    keywords = r['keywords']
        #    for keyword in keywords:
        #        wt = lookup(keyword.strip().lower())
        #        if wt != None and wt not in wikititles:
        #            wikititles.append(wt)
        cache[text] = wikititles
        if len(cache) > 10:
            cache = {}
    else:
        wikititles = cache[text]
    if verbose:
        old = set(f['wpterms'])
        print(findingID)
        for wt in wikititles:
            if wt not in old:
                print('+',wt)
        for wt in old:
            if wt not in wikititles:
                print('-',wt)
         
    collection.update({'_id': f['_id']},{'$unset': {'wpterms': 1 }}, multi=True)
    collection.update({'_id': f['_id']},{'$set':   {'wpterms': wikititles}}, upsert=False, multi=False)
