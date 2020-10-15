TMP=jardir
INSTALL=${HOME}
SHAR=enguage.shar

default:
	@echo "Usage: make [ install | enguage | shar | android | flatpak | clean ]" >&2

install: enguage

flatpak: enguage
	(cd app/flatpak; make install)

android: app/android.app/libs/anduage.jar

enguage: lib/enguage.jar

shar: ${SHAR}

${INSTALL}/etc:
	mkdir ${INSTALL}/etc

lib:
	mkdir lib

${TMP}:
	mkdir ${TMP}

${SHAR}: lib/enguage.jar
	echo "#!/bin/sh"                                                 >  ${SHAR}
	echo "SHAR=\`/bin/pwd\`/$$"0                                     >> ${SHAR}
	echo "cd $$"HOME                                                 >> ${SHAR}
	echo "awk '(y==1){print $$"0"}($$"1"==\"exit\"){y=1}' $$"SHAR \\ >> ${SHAR}
	echo "                                    | base64 -d | tar -xz" >> ${SHAR}
	echo "exit"                                                      >> ${SHAR}
	tar  -cz lib/enguage.jar etc bin/eng | base64                    >> ${SHAR}
	chmod +x ${SHAR}

uninstall:
	rm -rf ~/bin/eng ~/etc/config.xml ~/etc/rpt ~/lib/enguage.jar

lib/enguage.jar: ${TMP} lib
	cp -a org com ${TMP}
	mkdir -p ${TMP}/META-INF
	echo "Manifest-Version: 1.0"           >  ${TMP}/META-INF/MANIFEST.MF
	echo "Class-Path: ."                   >> ${TMP}/META-INF/MANIFEST.MF
	echo "Main-Class: org.enguage.Enguage" >> ${TMP}/META-INF/MANIFEST.MF
	( cd ${TMP} ;\
		find com org -name \*.java -exec rm -f {} \;  ;\
		find com org -name .DS_Store -exec rm -f {} \; ;\
		find com org -name .gitignore -exec rm -f {} \; ;\
		jar -cmf META-INF/MANIFEST.MF ../lib/enguage.jar META-INF org com \
	)
	rm -rf ${TMP}

app/android.app/libs/anduage.jar:
	mkdir -p ${TMP} app/android.app/libs
	cp -a org ${TMP}
	( cd ${TMP} ;\
		find org -name \*.java -exec rm -f {} \;  ;\
		find org -name .DS_Store -exec rm -f {} \; ;\
		find org -name .gitignore -exec rm -f {} \; ;\
		jar -cf ../app/android.app/libs/anduage.jar org \
	)
	rm -rf ${TMP}

clean:
	(cd app/flatpak; make clean)
	@rm -rf ${TMP} lib/ selftest/ variable var ${SHAR}
