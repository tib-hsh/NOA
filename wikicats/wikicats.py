import codecs
import json
import mysql.connector as mariadb
import re
import pprint
import argparse

argpar = argparse.ArgumentParser(description='Extract titles and category hierarchy form Wikipedia dump.')
argpar.add_argument('-full', action='store_true')
args = argpar.parse_args()
extract_titles = args.full


mariadb_connection = mariadb.connect(user='root', password='timewalk', database='wiki', charset = 'utf8')
cursor = mariadb_connection.cursor(buffered=True)
title2class = {}

metaclasspatterns_raw = {r'.*template',
                         r'Redirects',
                         r'(Un)?[Pp]rintworthy',
                         r'(All )?([Ww]ikipedia )?[Aa]rticles',
                         r'.*[Dd]isambiguation page',
                         r'.*[Ii]nfobox',
                         r'Use',
                         r'.*redirects to .*',
                         r'.* redirects',
                         r'Redirected .* articles',
                         r'Pages',
						 r'.* stubs',
                         r'.* category .* link .*',
						 r'Categories requiring diffusion',
						 r'.* categories',
						 r'Wikidata',
                         r'.* Wikipedia',
						 r'.* category maintenance',
						 r'.* categor.* tracking.*',
						 r'.* stubs',
                         }
                         
containerclasspatterns_raw = {r'.*1[0-9][0-9][0-9].*',
                         r'.*2[01][0-9][0-9].*',
                         r'Concepts in .*',
                         r'.* terminology',
                         r'Wikipedia.*',
                         r'List(s)? of .*',
                         r'Nothing',
                         r'Types of .*',
                         r'.* types',
                         r'.* by .*',
                         r'.* inventions',
                         r'.* disciplines',
                         r'Areas of.*',
                         r'Branches of.*',
                         r'Subfields of.*',
                         r'Research'
                         }
                         
nameclasspatterns_raw = {r'Physical quantities',
                         r'Physical constants',
                         r'.*[Ss]urnames',
                         r'.*[Gg]iven names',
                         r'Living people'
                         }
                         

                         
peopleclasspatterns_raw = {r'[0-9]* births',
                           r'[0-9]* deaths',
                           r'Living people',
                           r'Dead people'
                         }
                         
                         
metaclasspatterns = [re.compile(pattern) for pattern in metaclasspatterns_raw]
containerclasspatterns = [re.compile(pattern) for pattern in containerclasspatterns_raw]
nameclasspatterns = [re.compile(pattern) for pattern in nameclasspatterns_raw]
peopleclasspatterns = [re.compile(pattern) for pattern in peopleclasspatterns_raw]

def hidden_cats(curs):
    result = []
    curs.execute('''
select p.page_title
from page p, categorylinks cl 
where cl.cl_to = 'Hidden_categories' 
and p.page_id =  cl.cl_from   
and cl.CL_type = 'subcat' 
''')
    for line in curs:
        if isinstance(line[0], bytes):
            ctitle = line[0].decode('utf8').replace("_", " ")
        else:
            ctitle = line[0].replace("_", " ")
        result.append(ctitle)
    
    return result
    
    
def set_cats(curs):
    result = []
    curs.execute('''
select p.page_title
from page p, categorylinks cl 
where cl.cl_to = 'Set_categories' 
and p.page_id =  cl.cl_from   
and cl.CL_type = 'subcat' 
''')
    for line in curs:
        if isinstance(line[0], bytes):
            ctitle = line[0].decode('utf8').replace("_", " ")
        else:
            ctitle = line[0].replace("_", " ")
        result.append(ctitle)
    return result
    
   
def title_cat_pattern(title):
    title_class_words = ['albums','magazines','groups','bands','films','movies','seasons','series','songs','books','novels']
    for w in title_class_words:
        if w in title:
            return True
    return False
    
def title_cats(curs):
    titlecats = set(['Albums','Magazines','Musical groups','Films','Songs','Fiction books','Novels'])
    todo = set(['Albums','Magazines','Musical_groups','Films','Songs','Fiction_books','Novels'])
    #trace = set()
    

    while len(todo) > 0:
        hyper = todo.pop()
        
        curs.execute('''
select page_title 
from hypernyms
where cl_to = \''''+hyper.replace("'", "''")+'''\'
''')
        for line in curs:
            if isinstance(line[0], bytes):
                sub = line[0].decode('utf8')
            else:
                sub = line[0]

            if sub.replace("_", " ") not in titlecats:
                if title_cat_pattern(sub) or containercategory(sub):
                    todo.add(sub)
                #trace.add(sub.replace("_", " ") + " --> " + hyper)
                titlecats.add(sub.replace("_", " "))
    return titlecats

def metacategory(cat):
    if cat in hidden_categories:
        return True
    for pat in metaclasspatterns:
        if pat.match(cat):
            return True
    return False

def containercategory(cat):
    if cat in set_categories: 
        return True
    for pat in containerclasspatterns:
        if pat.match(cat):
            return True
    return False

def namecategory(cat):
    if cat in title_categories: 
        return True
    for pat in nameclasspatterns:
        if pat.match(cat):
            return True
    return False
    
def peoplecategory(cat):
    for pat in peopleclasspatterns:
        if pat.match(cat):
            return True
    return False
    
    

            
            
def build_joined_table(curs):
    curs.execute('''
CREATE TEMPORARY TABLE hypernyms
SELECT p.page_title, 
       cl.cl_to
FROM page p
INNER JOIN categorylinks cl ON p.page_id =  cl.cl_from
WHERE cl.CL_type = 'subcat' 
''')
    curs.execute('''
CREATE INDEX hyp_title ON hypernyms(page_title);
    ''')
    curs.execute('''
CREATE INDEX hyp_to ON hypernyms(cl_to);
    ''')

def hypernyms(curs,term):
    result = []
    
    if term == 'Main topic classifications':
        return []
    
    wterm = term.replace(" ", "_").replace("'","''") #.encode('utf8')
    if len(wterm) < 1:
        return []
    #print(wterm)
    curs.execute('''
select h.cl_to 
from hypernyms h
where h.page_title = \''''+wterm+'''\'
''')
    for line in curs:
        if isinstance(line[0], bytes):
            ctitle = line[0].decode('utf8').replace("_", " ")
        else:
            ctitle = line[0].replace("_", " ")
        result.append(ctitle)    
            
    return result
    
def build_hyperdict(curs,wc):
    todo = list(set([cat for clist in wc.values() for cat in clist]))
    hyperdict = {}
    shallow_hyperdict = {}

    while len(todo) > 0: 
        cat = todo.pop()
        hyper = hypernyms(curs,cat)
        if len(hyper) > 0:
            hypernymlist = []
            shallowhypernymlist = []
            for h in hyper:
                if metacategory(h) or namecategory(h):
                    continue
                
                if containercategory(h) or containercategory(cat):
                    shallowhypernymlist.append(h)
                else:
                    hypernymlist.append(h)
                    
                if h not in hyperdict and h not in shallow_hyperdict:
                    todo.append(h)
                    
            if not containercategory(cat):
                hyperdict[cat] = hypernymlist
                if len(shallowhypernymlist)  > 0 and len(hypernymlist) == 0: 
                    shallow_hyperdict[cat] = shallowhypernymlist
            else:
                shallow_hyperdict[cat] = shallowhypernymlist
                
    # if a category has no broader categories, take broader categories of a container category it belongs to
    for cat in hyperdict:
        if len(hyperdict[cat]) == 0:
            hypernymlist = []
            todo = [cat]
            while len(todo) > 0 and len(hypernymlist) == 0:
                c = todo.pop()
                for h in shallow_hyperdict.get(c,[]):
                    if containercategory(h):
                        todo.append(h)
                    else:
                        hypernymlist.append(h)
                
                
            hyperdict[cat] = hypernymlist
            #print(cat," --> ", hypernymlist)
            
    #for each category, remove all parents that are grantparents as well
    for cat in hyperdict:
        hypernymlist = hyperdict[cat]
        removed = False
        for h in hyperdict[cat]:
            for hh in hyperdict.get(h,[]):
                if hh in hypernymlist:
                    hypernymlist.remove(hh)
                    removed = True
                    #print(cat," - ", hh)
        if removed:
            hyperdict[cat] = hypernymlist
           
        
    
    return hyperdict            
  
def find_titles(curs):
    processed = 0
    offset = 0
    list_of_people = []
    global title2class
      
    curs.execute('''
select p.page_title, cl.cl_to  
from page p, categorylinks cl 
where p.page_id =  cl.cl_from and cl.cl_type = 'page' and p.page_namespace = 0 
''')

    for line in curs:
        processed += 1
        if processed%10000 == 0:
            print(processed)
            
        if isinstance(line[0], bytes):
            ptitle = line[0].decode('utf8').replace("_", " ")
        else:
            ptitle = line[0].replace("_", " ")
        
        if ptitle in list_of_people:
            continue
        if len(ptitle.split()) > 4:
            continue
            
        if isinstance(line[0], bytes):
            ctitle = line[1].decode('utf8').replace("_", " ")
        else:
            ctitle = line[1].replace("_", " ")
            
        if peoplecategory(ctitle):
            if ptitle in title2class:
                del title2class[ptitle]
            list_of_people.append(ptitle)         
        elif not metacategory(ctitle) and not containercategory(ctitle) and not namecategory(ctitle) and len(ptitle) > 2:
            hypernyms = title2class.get(ptitle, [])
            hypernyms.append(ctitle)
            title2class[ptitle] = hypernyms
              



hidden_categories = hidden_cats(cursor)
print(len(hidden_categories), "hidden categories")
set_categories = set_cats(cursor)
print(len(set_categories), "set categories")
build_joined_table(cursor)
title_categories = title_cats(cursor)
print(len(title_categories), "title categories")

if extract_titles:
	find_titles(cursor)
	print(len(title2class), "titles")
	data_file = codecs.open('wikicats.json','w','utf-8')
	data_file.write(json.dumps(title2class, indent=2))
	data_file.close()
else:
	with open("wikicats.json") as fin:
		title2class = json.loads(fin.read(),encoding="utf-8")

hyperdict = build_hyperdict(cursor,title2class)
print(len(hyperdict), "hypernyms")


fout = codecs.open("wikihypernym.json", "w","utf-8")
fout.write(json.dumps(hyperdict, indent=2))
fout.close()