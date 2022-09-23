FROM dockerimages-prd.jbhunt.com/jbh-alpine-jre11:latest
EXPOSE 8080
ADD target/listener_edi_sterlingarchive.jar listener_edi_sterlingarchive.jar
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.krb5.conf=/etc/krb5.conf -jar listener_edi_sterlingarchive.jar