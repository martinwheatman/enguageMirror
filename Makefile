TMP=tmp
MANIFEST=${TMP}/META-INF/MANIFEST.MF
INSTALL=${HOME}
SHAR=enguage.shar
ANDLIBS=${HOME}/StudioProjects/Enguage/app/libs

default:
	@echo "Usage: make [ snap | jar | shar | android | flatpak | clean ]" >&2

install: jar

flatpak: jar
	(cd opt/flatpak; make install)

android: ${ANDLIBS}/anduage.jar

shar: ${SHAR}

jar: lib/enguage.jar

lib/enguage.jar: ${MANIFEST} ${TMP} lib
	mkdir -p ${TMP}
	cp -a org com ${TMP}
	( cd ${TMP} ;\
		find com org -name \*.class -exec rm -f {} \; ;\
		find com org -name .DS_Store -exec rm -f {} \; ;\
		find com org -name .gitignore -exec rm -f {} \; ;\
		javac org/enguage/Enguage.java ;\
		find com org -name \*.java -exec rm -f {} \;  ;\
		jar -cmf META-INF/MANIFEST.MF ../lib/enguage.jar META-INF org com \
	)

${INSTALL}/etc:
	mkdir ${INSTALL}/etc
	
${TMP}:
	mkdir -p ${TMP}

lib:
	mkdir lib

${SHAR}: lib/enguage.jar
	echo "#!/bin/sh"                                                 >  ${SHAR}
	echo "SHAR=\`/bin/pwd\`/$$"0                                     >> ${SHAR}
	echo "cd $$"HOME                                                 >> ${SHAR}
	echo "awk '(y==1){print $$"0"}($$"1"==\"exit\"){y=1}' $$"SHAR \\ >> ${SHAR}
	echo "                                    | base64 -d | tar -xz" >> ${SHAR}
	echo "exit"                                                      >> ${SHAR}
	tar  -cz lib/enguage.jar etc bin/eng | base64                    >> ${SHAR}
	chmod +x ${SHAR}

snap: jar
	tar  -czf opt/snapcraft/enguage.tgz lib/enguage.jar etc bin/eng
	(cd opt/snapcraft; snapcraft)

uninstall:
	rm -rf ~/bin/eng ~/etc/config.xml ~/etc/rpt ~/lib/enguage.jar

${MANIFEST}:
	mkdir -p `dirname ${MANIFEST}`
	echo "Manifest-Version: 1.0"           >  ${MANIFEST}
	echo "Class-Path: ."                   >> ${MANIFEST}
	echo "Main-Class: org.enguage.Enguage" >> ${MANIFEST}

${ANDLIBS}/anduage.jar: ${TMP}
	mkdir -p ${ANDLIBS}
	cp -a org ${TMP}
	( cd ${TMP} ;\
		find org -name \*.java -exec rm -f {} \;  ;\
		find org -name .DS_Store -exec rm -f {} \; ;\
		find org -name .gitignore -exec rm -f {} \; ;\
		jar -cf ${ANDLIBS}/anduage.jar org \
	)

clean:
	(cd opt/flatpak; make clean)
	(cd opt/snapcraft; snapcraft clean; rm -f enguage.tgz enguage_*.snap)
	@rm -rf ${TMP} lib/ selftest/ variable var ${SHAR}
	find . -name "*.class" -exec /bin/rm {} \;
