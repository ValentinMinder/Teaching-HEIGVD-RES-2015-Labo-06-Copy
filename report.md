# Report for Lab 6: LDAP

## Table of Contents
1. [Introduction](#Intro)
1. [DIT Structure](#Struct)
1. [Mapping the CSV to DIT](#Map)
1. [Importing data](#Import)
1. [LDAP filter commands](#Filter)
1. [Dynamic group commands](#Group)
1. [Conclusion](#End)

# <a name="Intro"></a> Introduction

# <a name="Struct"></a> DIT Structure
[![](images/RES_6_DIT.png)](images/RES_6_DIT.png)

Our DIT uses the `dc=contacts,dc=heigvd,ch=ch` base.

This base then contains two organisational units: `People`, which stores the entries of all the people in the directory, and `Departments`, which stores groups of people in a specific department. This is done with a flat structure, with the department name stored in the entries in People, and the entries in Departments being dynamic groups that look for said department name.

# <a name="Map"></a> Mapping the CSV to DIT
[![](images/RES_6_CSV.png)](images/RES_6_CSV.png)

One line in the CSV file corresponds to one entry in the directory. The image above illustrates which fields of the CSV correspond to which field in the LDIF entry. They are all copied as-is, other than the `MALE/FEMALE` field, which is stored under `title`.

We use `departmentNumber` despite our departments being represented by letters because the field accepts all alphanumeric characters.

# <a name="Import"></a> Importing data

1. If the CSV file has not yet been generated, do so. We will be referring to it as `users.csv`
1. While in the `LdapDataGenerator` folder, run the command `python LDAPParser.py users.csv users.ldif`. The file `users.ldif` will be generated in the current directory. This LDIF file contains everything to set up our directory from scratch, and we only need to import it in OpenDJ now.
1. To import, use the command `sudo /opt/opendj/bin/import-ldif -h localhost -p 4444 -b dc=heigvd,dc=ch -l /vagrant/users.ldif -R /vagrant/rejected.ldif --skipFile /vagrant/skipped.ldif`

The `LDAPParser.py` script simply creates all the domains and organisational units for the directory, then parses through the CSV file to write each entry for the new users. Once done, it creates all the departments as dynamic groups according to which department names were read in the CSV entries.

# <a name="Filter"></a> LDAP filter commands

* What is the **number** (not the list!) of people stored in the directory?  
`./ldapsearch -p 389 -b "dc=heigvd, dc=ch" "ou=People" numsubordinates`

* What is the **number** of departments stored in the directory?  
`./ldapsearch -p 389 -b "dc=heigvd, dc=ch" "ou=Department" numsubordinates`

* What is the **list** of people who belong to the TIC Department?  
`./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "departmentNumber=TIC" cn`

* What is the **list** of students in the directory?  
`./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "employeeType=Etudiant" cn`

* What is the **list** of students in the TIC Department?  
`./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "(&(employeeType=Etudiant)(departmentNumber=TIC))" cn`

# <a name="Group"></a> Dynamic groupd commands

The following commands require first adding the organisational unit `Groups` under `dc=contacts,dc=heigvd,dc=ch`

* What command do you run to **define a dynamic group** that represents all members of the TIN Department?  
`Department?`  
`dn: cn=ExTIN,ou=Groups,dc=contacts,dc=heigvd,dc=ch`  
`cn: ExTIN`  
`objectClass: top`  
`objectClass: groupOfURLs`  
`ou: Groups`  
`memberURL: ldap:///ou=People,dc=heigvd,dc=ch??sub?departmentNumber=TIN`

* What command do you run to **get the list of all members of the TIN Department**?  
`./ldapsearch -p 389 -b "dc=contacts,dc=heigvd,dc=ch" "isMemberOf=cn=ExTIN,ou=Groups,dc=contacts,dc=heigvd,dc=ch" cn`

* What command do you run to **define a dynamic group** that represents all students with a last name starting with the letter 'A'?  
`dn: cn=StdA,ou=Groups,dc=contacts,dc=heigvd,dc=ch`  
`cn: StdA`  
`objectClass: top`  
`objectClass: groupOfURLs`  
`ou: Groups`  
`memberURL: ldap:///ou=People,dc=contacts,dc=heigvd,dc=ch??sub?(&(sn=A*)(employeeType=Etudiant))`

* What command do you run to **get the list** of these students?  
`./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "isMemberOf=cn=StdA,ou=Groups,dc=contacts,dc=heigvd,dc=ch" cn`

# <a name="End"></a> Conclusion

