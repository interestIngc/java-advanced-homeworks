mkdir _build

SET lib=..\..\..\..\..\..\lib2

set CP=%LIB%\junit-jupiter-api-5.4.2.jar;%LIB%\apiguardian-api-1.0.0.jar;^
%LIB%\junit-platform-engine-1.4.2.jar;%LIB%\junit-platform-commons-1.4.2.jar

javac -cp %CP% *.java -d _build