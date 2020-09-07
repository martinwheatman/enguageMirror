TMP=jardir

default:
	@echo "Usage: make [ jar ]" >&2

jar:
	mkdir ${TMP}
	cp -a org ${TMP}
	cp -a etc/META-INF ${TMP}
	(   cd ${TMP} ;\
	    find org -name \*.java -exec rm -f {} \;  ;\
	    find org -name .DS_Store -exec rm -f {} \; ;\
	    find org -name .gitignore -exec rm -f {} \; ;\
	    jar -cmf META-INF/MANIFEST.MF ../enguage.jar META-INF org \
    )
	rm -rf ${TMP}

clean:
	rm -rf enguage.jar variable var/uid

