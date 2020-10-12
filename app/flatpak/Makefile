INSTALL=${HOME}/java/enguage

default:
	echo "make [ install | export | clean ]"

install:
	mkdir -p etc/rpt
	cp -a ${INSTALL}/etc/rpt/*.txt   .
	cp -a ${INSTALL}/etc/config.xml  etc
	cp -a ${INSTALL}/lib/enguage.jar .
	flatpak-builder --force-clean --user --install bld org.enguage.Eng.yaml
	#rm -rf *.txt etc bld enguage.jar .flatpak-builder

export:
	mkdir -p etc/rpt
	cp -a ${INSTALL}/etc/rpt/*.txt   .
	cp -a ${INSTALL}/etc/config.xml  etc
	cp -a ${INSTALL}/lib/enguage.jar .
	flatpak-builder --force-clean bld org.enguage.Eng.yaml
	#rm -rf *.txt etc bld enguage.jar .flatpak-builder

clean:
	rm -rf .flatpak-builder *.txt enguage.jar bld var etc variable repo
