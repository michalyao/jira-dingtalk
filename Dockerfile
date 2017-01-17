FROM java:8u77-jre-alpine

MAINTAINER yaoyao<yaoyao0777@gmail.com>

ENV VERTICLE_HOME /opt/verticles

ENV VERTICLE_FILE dingtalk-jira-plugin-1.0.0-SNAPSHOT-fat.jar

EXPOSE 8200

COPY target/$VERTICLE_FILE  $VERTICLE_HOME

WORKDIR $VERTICLE_HOME

ENTRYPOINT ["sh", "-c"]

CMD ["java -jar $VERTICLE_FILE"]