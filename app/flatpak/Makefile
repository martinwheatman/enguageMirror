INSTALL=${HOME}

install: etc/rpt
	cp -a ${INSTALL}/etc/rpt/*.txt   .
	cp -a ${INSTALL}/etc/config.xml  etc
	cp -a ${INSTALL}/lib/enguage.jar .
	flatpak-builder --force-clean --user --install bld org.enguage.Eng.yaml

etc/rpt:
	mkdir -p etc/rpt

clean:
	rm -rf .flatpak-builder etc enguage.jar bld var variable repo *.txt


