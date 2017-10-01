#!/bin/bash
#title           :generateTest.sh
#description     :This script generates necessary folders, files and methods for
#                 the lightweight-rolez project
#author          :Michael Giger
#bash_version    :3.1.23(6)-release
#==============================================================================

#exit if no args are available
if [ $# -ne 1 ]
  then
    echo "No or too many arguments supplied"
    exit 1
fi


#generate necessary directories
mkdir testClasses/$1
mkdir testClasses/$1/classes

#generate necessary (empty) files
javaFileName="$(tr "[:lower:]" "[:upper:]" <<< ${1:0:1})${1:1}"

#generate testfile and stub
testClass=testClasses/$1/classes/${javaFileName}.java
touch testClass
echo -e "package classes;" >> $testClass
echo -e "" >> $testClass
echo -e "public class ${javaFileName} {" >> $testClass
echo -e "" >> $testClass
echo -e "\tpublic static void main(String[] args) {" >> $testClass
echo -e "" >> $testClass
echo -e "\t}" >> $testClass
echo -e "}" >> $testClass

#generate reference file
touch testClasses/$1/expected.out

#add method to TransformerTest.java
testFile="src/test/java/test/TransformerTest.java"

#remove last bracket
sed -i '$d' ${testFile}

echo -e "" >> $testFile
echo -e "\t@Test" >> $testFile
echo -e "\tpublic void $1() {" >> $testFile
echo -e "\t\tString methodName = \"$1\";" >> $testFile
echo -e "\t\tString mainClass = \"classes.${javaFileName}\";" >> $testFile
echo -e "\t\tPipeline p = new Pipeline(methodName, mainClass);" >> $testFile
echo -e "\t\tp.run(true, false);" >> $testFile
echo -e "\t}" >> $testFile

#add last bracket
echo -e "}" >> $testFile