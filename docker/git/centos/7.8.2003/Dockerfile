FROM centos:7.8.2003

RUN yum remove git*

# git version 2.24.1 (2020/10/01)
RUN yum -y install https://packages.endpoint.com/rhel/7/os/x86_64/endpoint-repo-1.7-1.x86_64.rpm \
 && yum -y install git
