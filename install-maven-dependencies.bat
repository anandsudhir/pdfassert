@echo off
cls
color 1F
echo.
echo.
echo.
echo     ����������������������������������������������������ͻ
echo     �                                                    �
echo     �                  ***PDFAssert***                   �
echo     �                                                    �
echo     �           Installing jars to local repo            �
echo     �                                                    �
echo     �                                                    �
echo     ����������������������������������������������������ͼ
echo.
echo.
echo.


echo Installing jars to local repo...

`mvn install:install-file -Dfile=lib/icepdf-core.jar -DgroupId=org.icepdf -DartifactId=icepdf-core -Dversion=4.3.3 -Dpackaging=jar`

`mvn install:install-file -Dfile=lib/diff_match_patch-current.jar -DgroupId=diff_match_patch -DartifactId=diff_match_patch -Dversion=current -Dpackaging=jar -Dclassifier=sources`

`mvn install:install-file -Dfile=lib/icepdf-viewer.jar -DgroupId=org.icepdf -DartifactId=icepdf-viewer -Dversion=4.3.3 -Dpackaging=jar`


# mvn install:install-file -Dfile=lib/diff_match_patch-current.jar -DgroupId=diff_match_patch -DartifactId=diff_match_patch -Dversion=current -Dpackaging=jar