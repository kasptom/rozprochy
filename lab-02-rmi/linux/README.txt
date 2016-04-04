wymagania:
java version "1.8.0_77"
Java(TM) SE Runtime Environment (build 1.8.0_77-b03)
Java HotSpot(TM) 64-Bit Server VM (build 25.77-b03, mixed mode)

1. kompilacja (folder rmi)
 javac *.java

2. uruchamianie (w folderze rmi)
	1) uruchamianie rejestru:	rmiregistry 1099
	2) uruchamianie serwera:	java -cp . -Djava.rmi.server.codebase=file:./INoteBoard.jar NoteBoardServer 127.0.0.1 1099
	3) uruchamianie klienta:	java -cp . Main zenek 127.0.0.1 1099 real


