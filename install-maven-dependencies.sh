#!/bin/bash

echo
echo
echo
echo     浜様様様様様様様様様様様様様様様様様様様様様様様様様融
echo     �                                                    �
echo     �                  ***PDFAssert***                   �
echo     �                                                    �
echo     �           Installing jars to local repo            �
echo     �                                                    �
echo     �                                                    �
echo     藩様様様様様様様様様様様様様様様様様様様様様様様様様夕
echo
echo
echo


echo Installing jars to local repo...

mvn install:install-file -Dfile=lib/icepdf-core.jar -DgroupId=org.icepdf -DartifactId=icepdf-core -Dversion=4.3.3 -Dpackaging=jar

mvn install:install-file -Dfile=lib/diff_match_patch-current-src.jar -DgroupId=diff_match_patch -DartifactId=diff_match_patch -Dversion=current -Dpackaging=jar -Dclassifier=sources

mvn install:install-file -Dfile=lib/icepdf-viewer.jar -DgroupId=org.icepdf -DartifactId=icepdf-viewer -Dversion=4.3.3 -Dpackaging=jar


# mvn install:install-file -Dfile=lib/diff_match_patch-current.jar -DgroupId=diff_match_patch -DartifactId=diff_match_patch -Dversion=current -Dpackaging=jar
