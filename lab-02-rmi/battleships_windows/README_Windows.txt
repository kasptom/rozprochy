wymagania:
java version "1.8.0_77"
Java(TM) SE Runtime Environment (build 1.8.0_77-b03)
Java HotSpot(TM) 64-Bit Server VM (build 25.77-b03, mixed mode)

Wszystkie czynnosci wykonywane w folderze z plikami (przykladowo: C:\battleships_windows)

Kompilacja:
javac *.java

REJESTR
rmiregistry 1099 -J-Djava.rmi.server.codebase=file:///C:/battleships_windows/

SERWER
java -cp . -Djava.rmi.server.codebase=file:/c:/battleships_windows/ -Djava.server.hostname=MSI-PC -Djava.security.policy=server.policy NoteBoardServer 127.0.0.1 1099


KLIENT
java -cp . Main zenek 127.0.0.1 1099 real


