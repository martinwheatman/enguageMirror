TMP=jardir
INSTALL=${HOME}

default:
	@echo "Usage: make [ install | uninstall | android | flatpak | clean ]" >&2

flatpak: install
	(cd app/flatpak; make install)

android: app/android.app/libs/anduage.jar

install: \
	${INSTALL}/etc/rpt \
	${INSTALL}/etc/config.xml \
	${INSTALL}/lib/enguage.jar \
	app/android.app/libs/anduage.jar

uninstall:
	rm -rf ${INSTALL}/etc/rpt
	rm -f  ${INSTALL}/etc/config.xml
	rm -f  ${INSTALL}/lib/enguage.jar
	rm -f  app/android.app/libs/anduage.jar

${INSTALL}/etc/config.xml: ${INSTALL}/etc
	cp etc/config.xml    ${INSTALL}/etc

${INSTALL}/etc/rpt: ${INSTALL}/etc
	cp -a etc/rpt        ${INSTALL}/etc

${INSTALL}/lib/enguage.jar: ${INSTALL}/lib enguage.jar
	cp enguage.jar       ${INSTALL}/lib

${INSTALL}/etc:
	mkdir ${INSTALL}/etc

${INSTALL}/lib:
	mkdir ${INSTALL}/lib

enguage.jar:
	mkdir ${TMP}
	cp -a org com etc/META-INF ${TMP}
	( cd ${TMP} ;\
		find com org -name \*.java -exec rm -f {} \;  ;\
		find com org -name .DS_Store -exec rm -f {} \; ;\
		find com org -name .gitignore -exec rm -f {} \; ;\
		jar -cmf META-INF/MANIFEST.MF ../enguage.jar META-INF org com \
	)
	rm -rf ${TMP}

app/android.app/libs/anduage.jar:
	mkdir ${TMP}
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
	@rm -rf enguage.jar selftest/ variable var/uid
