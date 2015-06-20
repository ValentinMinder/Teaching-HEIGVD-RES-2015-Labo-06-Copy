from __future__ import print_function
import sys
import re
from sets import Set

def p(type,name):
    global output
    print(type + ": " + name, file=output)

def poc(name):
    p("objectClass",name)

def pn():
    global output
    print("", file=output)

def pou(name):
    global domain
    p("dn", "ou=" + name + "," + domain)
    poc("organizationalunit")
    poc("top")
    p("ou", name)
    pn()
    
def pdg(name,dpt):
    global domain
    p("dn", "cn=" + name + ",ou=Departments," + domain)
    p("cn", name)
    poc("top")
    poc("groupOfURLs")
    p("ou", "Departments")
    p("memberURL", "ldap:///ou=People," + domain + "??sub?departmentNumber=" + dpt)
    pn()

if sys.argv[1] and sys.argv[2]:
    input = open(sys.argv[1],'r')
    output = open(sys.argv[2], 'w')
else:
    print("Error in argument number")
    sys.exit()

domain = "dc=contacts,dc=heigvd,dc=ch"

p("dn", "dc=heigvd,dc=ch")
poc("domain")
poc("top")
p("dc","heigvd")
pn()

p("dn", domain)
poc("domain")
poc("top")
p("dc","contacts")
pn()

pou("People")
pou("Departments")

# 0 EID_100001,
# 1 Dupond,
# 2 Luc,
# 3 (024) 777 373 170,
# 4 luc.dupond@heig-vd.ch,
# 5 MALE,
# 6 HEG,
# 7 Etudiant

dpts = Set()

for line in input:
    s = re.split(", ",line)
    if len(s) == 8:
        p("dn", "uid=" + s[0] + ",ou=People," + domain)
        poc("top")
        poc("person")
        poc("organizationalPerson")
        poc("inetOrgPerson")
        p("uid", s[0])
        p("sn", s[1])
        p("givenName", s[2])
        p("cn", s[2] + " " + s[1])
        p("telephoneNumber", s[3])
        p("mail", s[4])
        if "FEMALE" in s[5]:
            p("title","Ms")
        else:
            p("title","Mr")
        p("departmentNumber", s[6])
        p("employeeType", s[7])
        dpts.add(s[6])

for d in dpts:
    pdg("Dpt" + d, d)