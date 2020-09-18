TMP=jardir
INSTALL=${HOME}

default:
	@echo "Usage: make [ jar | eng | install | clean ]" >&2

install: ${INSTALL}/bin ${INSTALL}/etc ${INSTALL}/lib eng jar
	cp sbin/eng ${INSTALL}/bin/
	cp -a etc/config.xml ${INSTALL}/etc
	cp -a etc/rpt        ${INSTALL}/etc
	cp enguage.jar       ${INSTALL}/lib

${INSTALL}/bin:
	mkdir ${INSTALL}/bin

${INSTALL}/etc:
	mkdir ${INSTALL}/etc

${INSTALL}/lib:
	mkdir ${INSTALL}/lib

eng: sbin/eng

sbin/eng: src/eng/eng.c
	(cd src/eng/; make install)

jar:
	mkdir ${TMP}
	cp -a org ${TMP}
	cp -a com ${TMP}
	cp -a etc/META-INF ${TMP}
	( cd ${TMP} ;\
		find com -name \*.java -exec rm -f {} \;  ;\
		find org -name \*.java -exec rm -f {} \;  ;\
		find com -name .DS_Store -exec rm -f {} \; ;\
		find org -name .DS_Store -exec rm -f {} \; ;\
		find org -name .gitignore -exec rm -f {} \; ;\
		find com -name .gitignore -exec rm -f {} \; ;\
		jar -cmf META-INF/MANIFEST.MF ../enguage.jar META-INF org com \
  )
	rm -rf ${TMP}

clean:
	(cd src/eng; make clean)
	rm -rf enguage.jar selftest/ variable var/uid sbin/eng
