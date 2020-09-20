TMP=jardir
INSTALL=${HOME}

default:
	@echo "Usage: make [ install | uninstall | flatpak | clean ]" >&2

flatpak: install
	(cd app/flatpak; make install)

install: \
	${INSTALL}/etc/rpt \
	${INSTALL}/etc/config.xml \
	${INSTALL}/lib/enguage.jar \
	${INSTALL}/bin/eng

uninstall:
	rm -rf ${INSTALL}/etc/rpt
	rm -f  ${INSTALL}/etc/config.xml
	rm -f  ${INSTALL}/lib/enguage.jar
	rm -f  ${INSTALL}/bin/eng

${INSTALL}/bin/eng: ${INSTALL}/bin sbin/eng 
	cp sbin/eng          ${INSTALL}/bin/

${INSTALL}/etc/config.xml:
	cp etc/config.xml    ${INSTALL}/etc

${INSTALL}/etc/rpt:
	cp -a etc/rpt        ${INSTALL}/etc

${INSTALL}/lib/enguage.jar: enguage.jar
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

enguage.jar:
	mkdir ${TMP}
	cp -a org ${TMP}
	cp -a com ${TMP}
	cp -a etc/META-INF ${TMP}
	( cd ${TMP} ;\
		find com org -name \*.java -exec rm -f {} \;  ;\
		find com org -name .DS_Store -exec rm -f {} \; ;\
		find com org -name .gitignore -exec rm -f {} \; ;\
		jar -cmf META-INF/MANIFEST.MF ../enguage.jar META-INF org com \
	)
	rm -rf ${TMP}

clean:
	(cd app/flatpak; make clean)
	(cd src/eng;     make clean)
	@rm -rf enguage.jar selftest/ variable var/uid sbin/eng
