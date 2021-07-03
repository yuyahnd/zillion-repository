ARG PYTHON_VERSION
FROM python:${PYTHON_VERSION}

RUN apt-get update && apt-get install -y locales \
 && localedef -f UTF-8 -i ja_JP ja_JP.UTF-8
