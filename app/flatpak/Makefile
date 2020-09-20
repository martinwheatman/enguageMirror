INSTALL=${HOME}

install:
	cp -a ${INSTALL}/etc/rpt        etc
	cp -a ${INSTALL}/etc/config.xml etc
	cp -a ${INSTALL}/enguage.jar    .
	cp -a ${INSTALL}/bin/eng        .
	flatpak-builder --force-clean --user --install build org.enguage.Eng.yaml

clean:
	rm -rf etc eng enguage.jar build vat variable

