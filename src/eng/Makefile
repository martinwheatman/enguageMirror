install: ../../sbin/eng

../../sbin/eng: ../../sbin eng
	cp eng ../../sbin

../../sbin:
	mkdir ../../sbin
	
eng: eng.c
	gcc -o eng eng.c

clean:
	rm -f eng *.o
