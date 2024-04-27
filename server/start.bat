docker stop test
docker build --tag test .
docker run --rm -d --name test -v "C:\Users\Alex Wang\git\yt\app":/opt/app -p 3000:3000  test  
docker exec -it test /bin/bash