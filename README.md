# Report for Lab 6: LDAP

## Table of Contents
1. [Introduction](#Intro)
1. [DIT Structure](#Struct)
1. [Mapping the CSV to DIT](#Map)
1. [Prerequisite: Vagrant and OpenDJ](#GetStarted)
1. [Importing data](#Import)
1. [Field correspondance: new attribute and class](#Field)
1. [LDAP filter commands](#Filter)
1. [Dynamic group commands](#Group)
1. [Issues](#Issues)
1. [Conclusion](#End)


## <a name="Intro"></a> Introduction

The purposes of this lab is to manage a LDAP directoy with an OpenDJ server running on a Vagrant virtual box. It will solve the Lab 6 proposed in the [instructions](https://github.com/ValentinMinder/Teaching-HEIGVD-RES-2015-Labo-06-Copy/blob/master/LabInstructions.md).

We first have to define how the directory is organized, and then we have to import data from a `.csv` file to the directory, with all step needed explicitely showed in details.

This lab was done in June 2015 by [Eléonore d'Agostino](https://github.com/paranoodle) and [Valentin Minder](https://github.com/ValentinMinder) in the [Network Course](https://github.com/wasadigi/Teaching-HEIGVD-RES) at HEIG-VD / HES-SO given by [Olivier Liecthi](https://github.com/wasadigi). 

[HES-SO](http://www.hes-so.ch/) is the french acronym for University of Applied Sciences Western Switzerland (Haute Ecole Spécialisée de Suisse Occidentale). [HEIG-VD](http://heig-vd.ch/) is the french acronym for School of Engineering and Business State of Vaud (Haute Ecole d'Ingéniérie de l'Etat de Vaud).

## <a name="Struct"></a> DIT Structure
[![Hello](images/RES_6_DIT.png)](images/RES_6_DIT.png)

Our DIT uses the `dc=contacts,dc=heigvd,ch=ch` base.

This base then contains two organisational units: `People`, which stores the entries of all the people in the directory, and `Departments`, which stores groups of people in a specific department. This is done with a flat structure, with the department name stored in the entries in People, and the entries in Departments being dynamic groups that look for said department name.

As you will see later, we will add a third node at `OrganizationalUnit` level, namely `Groups`, that will contain all dynamic groups created aftewards. Please note that departments are already dynamic groups, but we will define all of them at importation time.

There is the *base* `ldif` commands that creates the above empty structure:

```
dn: dc=heigvd,dc=ch
objectClass: domain
objectClass: top
dc: heigvd

dn: dc=contacts,dc=heigvd,dc=ch
objectClass: domain
objectClass: top
dc: contacts

dn: ou=People,dc=contacts,dc=heigvd,dc=ch
objectClass: organizationalunit
objectClass: top
ou: People

dn: ou=Departments,dc=contacts,dc=heigvd,dc=ch
objectClass: organizationalunit
objectClass: top
ou: Departments
```

## <a name="Map"></a> Mapping the CSV to DIT
[![](images/RES_6_CSV.png)](images/RES_6_CSV.png)

One line in the CSV file corresponds to one entry in the directory. The image above illustrates which fields of the CSV correspond to which field in the LDIF entry. They are all copied as-is, other than the `MALE/FEMALE` field, which is stored under `title`.

We use `departmentNumber` despite our departments being represented by letters because the field accepts all alphanumeric characters.

## <a name="GetStarted"></a> Prerequisite: Vagrant and OpenDJ

First, clone this repository:
[Vagrant - OpenDJ repository](https://github.com/ValentinMinder/Teaching-HEIGVD-RES-2015-OpenDJ)

Open a terminal, go in the `box` folder, type `vagrant up` (it will install everything needed and launch the vagrant virtual machine, it might take a few minutes the first time) and then `vagrant ssh` (to open a vagrant terminal). Move to `cd /opt/opendj/bin`, where all openDJ commands are now located. Type `sudo ./start-ds` to start the directory. You have to prefix all your LDAP/LDIF command by `./` to execute them, or add `/opt/opendj/bin` to the PATH of your virtual machine.

Example:

[![](screenshots/Screenshot%202015-06-20%2015.41.51.png)](screenshots/Screenshot%202015-06-20%2015.41.51.png)

At the end, don't forget to type `sudo ./stop-ds` to stop the directory when you're done. To exit vagrant, just type `exit`. To kill vagrant from you host machine, type `vagrant halt` from the `box` directory.

Additional information can be found in the lectures from Olivier Liecthi:
[Lectures notes](https://github.com/wasadigi/Teaching-HEIGVD-RES/blob/master/lectures/06-Lecture6-LDAP.md)
and
[PDF Slides](https://github.com/wasadigi/Teaching-HEIGVD-RES/blob/master/slides/06-LDAP.pdf)

## <a name="Import"></a> Importing data

1. If the CSV file has not yet been generated, do so by running the `DataGenerator.java` main method (you can find it in the current repo, in the `LdapDataGenerator` folder). We will be referring to it as `users.csv`
1. While in the `LdapDataGenerator` folder, run the command `python LDAPParser.py users.csv users.ldif`. The file `users.ldif` will be generated in the current directory. This LDIF file contains everything to set up our directory from scratch, and we only need to import it in OpenDJ now. The `LDAPParser.py` script simply creates all the domains and organisational units for the directory, then parses through the CSV file to write each entry for the new users. Once done, it creates all the departments as dynamic groups according to which department names were read in the CSV entries.
1. Alternatively, if you prefer `java`, just run the main method of `DataReaderFromCSVToLDIF.java`, and it will have the exact same effect.
1. To import the LDIF file in OpenDJ, use the following command (this time, in the OpenDJ repo, in the vagrant terminal)

```
sudo /opt/opendj/bin/import-ldif -h localhost -p 4444 -b dc=heigvd,dc=ch -l /vagrant/users.ldif
-R /vagrant/rejected.ldif --skipFile /vagrant/skipped.ldif
```

Execution:

[![](screenshots/Screenshot%202015-06-20%2015.43.55.png)](screenshots/Screenshot%202015-06-20%2015.43.55.png)


Options `-R` and `--skipFile` indicate what to do with problematic entries. If both `skipped.ldif` and `rejected.ldif` are empty after this operation, it means that everything went well (as we can see in these screenshots).


[![](screenshots/Screenshot%202015-06-20%2015.44.24.png)](screenshots/Screenshot%202015-06-20%2015.44.24.png)
[![](screenshots/Screenshot%202015-06-20%2015.44.37.png)](screenshots/Screenshot%202015-06-20%2015.44.37.png)


Documentation about *LDAP Data Interchange Format* (LDIF) can be found on the following websites:

[Documentation about LDIF (Oracle)](https://docs.oracle.com/cd/E19313-01/817-7616/ldif.html)

[Documentation and example of LDIF (ForgeRock/OpenDJ)](http://opendj.forgerock.org/Example.ldif)

## <a name="Field"></a> Field correspondance: new attribute and class

LDAP defines by default a few classes, and among them are person, organizationalPerson, and inetOrgPerson. They declare, among others, the following attributes (only those that are of interest for this lab are shown):

* **person**: *sn, cn, telephoneNumber, description*
* **organizationalPerson**: *title*
* **inetOrgPerson**: *departmentNumber, employeeType, mail, givenName, uid*

In our context, we are missing a **gender field**. Of course, we could write in the *description* field something like `SEX=MALE`, but that wouldn't be very nice and clean. We could also write it in the *title* field (which is what we actually did). This works if we only use *Sir* and *Madam* (or *Mr* and *Ms*), but what if we need to have the title of *Professor*? We wouldn't be able to differentiate between them properly. The only way to have a real *gender* field would be to declare it, and then create a subclass of *inetOrgPerson* that can have a *gender* field. 

This can be done with the following command, which gives you directory management edition, so that you can edit the schema:

```
ldapmodify -D "cn=directory manager" -p 389 -a
```
Enter, and then, to create the gender attribute in the directory manager:

```
dn: cn=schema
changetype: modify
add: attributeTypes
attributeTypes: ( 2.25.128424792425578037463837247958458780603.1
        NAME 'gender'
        EQUALITY caseIgnoreMatch
        SUBSTR caseIgnoreSubstringsMatch
        SYNTAX 1.3.6.1.4.1.1466.115.121.1.15{1024} )
```

Enter, and then, to **create the heigPerson class**, a subclass of *inetOrgPerson* with optional gender attribute:
```
dn: cn=schema
changetype: modify
add: objectClasses
objectClasses: ( 2.25.128424792425578037463837247958458780603.2
    NAME 'heigPerson'
    DESC 'heigPerson'
    SUP inetOrgPerson
    STRUCTURAL
    MAY  (gender)
 )
 ``` 
And you're done!

Various sources:
[Example of LDIF (ForgeRock/OpenDJ)](http://opendj.forgerock.org/Example.ldif) / 
[Add Elements to Schema (Apache)](https://directory.apache.org/apacheds/basic-ug/2.3.1-adding-schema-elements.html) / 
[LDIF Appendix (Oracle)](http://docs.oracle.com/cd/B14099_19/idmanage.1012/b15883/ldif_appendix003.htm) /
[New Object Class (NetIQ )](https://www.netiq.com/documentation/edir88/edir88tshoot/data/bq0e8ou.html) /
[Appendix E: LDAP - Object Classes and Attributes (Zytrax.com)](http://www.zytrax.com/books/ldap/ape/) /
[Custom Attribute Example (Hasini Gunasinghe)](http://hasini-gunasinghe.blogspot.ch/2011/02/how-to-introduce-custom-attributes-to.html)

## <a name="Filter"></a> LDAP filter commands

These are the answers to the question asked in the lab.

* What is the **number** (not the list!) of people stored in the directory?  
```
./ldapsearch -p 389 -b "dc=heigvd, dc=ch" "ou=People" numsubordinates
```

There are 10000 people in the directory. In the screenshot there are only 3000 because we worked on a subset of the data, as explained in the last section of this report.

[![](screenshots/Screenshot%202015-06-20%2015.46.29.png)](screenshots/Screenshot%202015-06-20%2015.46.29.png)

As you can see in the screenshot, despite its name, the `--countEntries` option is useless for this question.

* What is the **number** of departments stored in the directory?  
```
./ldapsearch -p 389 -b "dc=heigvd, dc=ch" ""(&(objectClass=OrganizationalUnit)(ou=Departments))"" numsubordinates
```
There are 6 departments (see above screenshot). We have to specify `objectClass=OrganizationalUnit` because otherwise it will also count subordinates of actual *Departements*, which will be 0, not only the subordinates of *OrganizationalUnit* above it, which is the only interesting thing.

* What is the **list** of people who belong to the TIC Department?  
```
./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "departmentNumber=TIC" cn
```

Results (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2015.47.56.png)](screenshots/Screenshot%202015-06-20%2015.47.56.png)

Results with details (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2015.51.18.png)](screenshots/Screenshot%202015-06-20%2015.51.18.png)

* What is the **list** of students in the directory?  
```
./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "employeeType=Etudiant" cn
```

Results (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2015.51.51.png)](screenshots/Screenshot%202015-06-20%2015.51.51.png)

Results with details (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2015.52.30.png)](screenshots/Screenshot%202015-06-20%2015.52.30.png)


* What is the **list** of students in the TIC Department?  
```
./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "(&(employeeType=Etudiant)(departmentNumber=TIC))" cn
```

Results (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2015.52.57.png)](screenshots/Screenshot%202015-06-20%2015.52.57.png)

Results with details (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2015.53.22.png)](screenshots/Screenshot%202015-06-20%2015.53.22.png)


## <a name="Group"></a> Dynamic group commands

The following commands require first adding the organisational unit `Groups` under `dc=contacts,dc=heigvd,dc=ch`. This can be done at importation time in `.ldif` file, or later with `ldapmodify`:

```
dn: ou=Groups,dc=contacts,dc=heigvd,dc=ch
objectClass: organizationalunit
objectClass: top
ou: Groups
```

You have two choices when creating dynamic groups. You can create them directly in the `.ldif` importation file, and they will be imported with the actual data. The other option is to "alter" the directory afterwards with `ldapmodify`. In this lab we did both once.

* What command do you run to **define a dynamic group** that represents all members of the TIN Department?  

Add the following in the `.ldif` file or in a `ldapmodify` command. In our lab, we choose to declare all departments in the `ldif` file as they can all be known at the end of the parsing.
```
dn: cn=DptTIN,ou=Departements,dc=contacts,dc=heigvd,dc=ch
cn: DptTIN
objectClass: top
objectClass: groupOfURLs
ou: Departments
memberURL: ldap:///ou=People,dc=contacts,dc=heigvd,dc=ch??sub?departmentNumber=TIN
```

* What command do you run to **get the list of all members of the TIN Department**?  
```
./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "isMemberOf=cn=DptTIN,ou=Groups,dc=contacts,dc=heigvd,dc=ch" cn
```

Results (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2015.54.11.png)](screenshots/Screenshot%202015-06-20%2015.54.11.png)

Results with details (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2015.54.52.png)](screenshots/Screenshot%202015-06-20%2015.54.52.png)

* What command do you run to **define a dynamic group** that represents all students with a last name starting with the letter 'A'?  


Add the following in the `.ldif` file or in a `ldapmodify` command. 

```
dn: cn=StdA,ou=Groups,dc=contacts,dc=heigvd,dc=ch
cn: StdA
objectClass: top  
objectClass: groupOfURLs  
ou: Groups
memberURL: ldap:///ou=People,dc=contacts,dc=heigvd,dc=ch??sub?(&(sn=A*)(employeeType=Etudiant))
```

* What command do you run to **get the list** of these students?  
```
./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "isMemberOf=cn=StdA,ou=Groups,dc=contacts,dc=heigvd,dc=ch" cn
```
Results:

[![](screenshots/Screenshot%202015-06-20%2015.55.13.png)](screenshots/Screenshot%202015-06-20%2015.55.13.png)

As you can see, they are no results in this group. Unfortunately they are no surnames starting with A at all. Therefore we started again with another letter... Below you can find the command creating the dynamic group that represents all students with a given name starting with the letter 'E'. As it was unexpected, we choose to add this one dynamically with `ldapmodify`, with the following command:
```
ldapmodify -D "cn=directory manager" -p 389 -a
```

Then enter this text, followed by two Enter.

```
dn: cn=StdGivenNameE,ou=Groups,dc=contacts,dc=heigvd,dc=ch
cn: StdGivenNameE
objectClass: top  
objectClass: groupOfURLs  
ou: Groups
memberURL: ldap:///ou=People,dc=contacts,dc=heigvd,dc=ch??sub?(&(givenName=E*)(employeeType=Etudiant))
```
[![](screenshots/Screenshot%202015-06-20%2016.28.38.png)](screenshots/Screenshot%202015-06-20%2016.28.38.png)

And the corresponding search query.
```
./ldapsearch -p 389 -b "dc=heigvd,dc=ch" "isMemberOf=cn=StdGivenNameE,ou=Groups,dc=contacts,dc=heigvd,dc=ch" cn
```
Results (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2016.29.29.png)](screenshots/Screenshot%202015-06-20%2016.29.29.png)

Results with details (only first results are shown):

[![](screenshots/Screenshot%202015-06-20%2016.30.04.png)](screenshots/Screenshot%202015-06-20%2016.30.04.png)


## <a name="Issues"></a> Issues
When working on the lab, we were always working in parallel on small subsets to test the commands. It's only at the end of the lab that we tested everything "in a row": extraction, importation, and queries, with the real data. That's where we encoutered a big problem: after the successful importation, none of the queries were working anymore. We got the following error.

```
LDAP_INSUFFICIENT_ACCESS
```

According to the documentation and various sources, this indicates that "the caller does not have sufficient rights to perform the requested operation". It was quite unexpected given that it was working perfectly with smaller subsets of data.

We soon figured out that it was an LDAP limitation. It seems regular users cannot make requests on unindexed elements when there are more then 4000 elements, apparently for efficiency reasons. So we tried almost everything, to make the request as admin, to make the limit bigger, or to find any workaround. After 2 hours of hard work, we gave up, and decided to finish the lab with only 3000 elements, to ensure everything else worked correctly.

To find out more about this issue:

[Forgerock documentation about admin privilegs in LDAP](http://docs.forgerock.org/en/opendj/2.6.0/admin-guide/#about-privileges)

[Oracle Blog talking about this issue](https://blogs.oracle.com/kanthi/entry/ldap_paged_results_more)

## <a name="End"></a> Conclusion
This lab was a good occasion to see how directories are handled in the real world with open-source programs. We encoutered some issues, some of whichs we solved after googling and reading documentation, and some other left unresolved. In general, it was interesting to take a problem from scratch and to manage it to the end.

General documentation about OpenDJ:

[Forgerock OpenDJ documentation](http://opendj.forgerock.org/opendj-server/doc/bootstrap/admin-guide/index.html)

[OpenDS documentation](https://opends.java.net/)
