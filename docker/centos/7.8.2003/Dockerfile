FROM centos:7.8.2003

RUN yum install -y yum-utils \
 && yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo \
 && yum -y install docker-ce docker-ce-cli containerd.io \
