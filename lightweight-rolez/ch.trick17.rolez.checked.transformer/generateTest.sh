#generate necessary directories
mkdir testClasses/$1
mkdir testClasses/$1/classes

#generate necessary (empty) files
javaFileName="$(tr "[:lower:]" "[:upper:]" <<< ${1:0:1})${1:1}"
touch testClasses/$1/classes/${javaFileName}.java
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