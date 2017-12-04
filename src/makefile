JFLAGS = -g 
#-Xlint
id=1
n=3
bearer=true
JC = javac
JVM = java
PUERTO = 12345
POLITICA = -Djava.security.policy
ARCHIVO = permisos
DIRECCION = localhost

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
	Inter.java \
	Client.java \
	Token.java \
	Server.java \

MAIN = Server

#default: classes
#	$(JVM) $(MAIN)

classes: $(CLASSES:.java=.class)

run: classes	
ifeq ($(MAIN),Client)
	$(JVM) $(POLITICA)=$(ARCHIVO) $(MAIN) $(id) $(n) $(bearer)
else
	$(JVM) $(POLITICA)=$(ARCHIVO) $(MAIN) $(PUERTO) 
endif	

clean:
	$(RM) *.class

