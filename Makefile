TMP=jardir
INSTALL=${HOME}

default:
	@echo "Usage: make [ install | android | flatpak | clean ]" >&2

flatpak: install
	(cd app/flatpak; make install)

android: app/android.app/libs/anduage.jar

install: lib/enguage.jar

${INSTALL}/etc:
	mkdir ${INSTALL}/etc

lib:
	mkdir lib

${TMP}:
	mkdir ${TMP}

lib/enguage.jar: ${TMP} lib
	cp -a org com etc/META-INF ${TMP}
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
	@rm -rf ${TMP} lib/ _user/ \
		selftest/ variable var/uid
