## Containerised

Obtain the repository from docker hub: martinwheatman/enguage

docker run -p 8080:8080 --mount type=volume,src=eng,dst=/var/local/eng --name persona enguage

Then on your browser:  localhost:8080/what do i need
