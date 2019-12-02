SpringBoot pacakge
==================

Reference
=========
* <https://my.oschina.net/nxxYqmvPOvsfH/blog/1789166>
* <https://www.jianshu.com/p/3bd850fcb488>

Case
============
Run by Systemd as below config:
```
[Unit]
Description=Spring Boot- Parser for telemetry
After=network.target kafka.service hbase.service
[Service]
ExecStart=/opt/nsp/os/jre/bin/java -jar /opt/toop/telemetry/parser-1.0.0-SNAPSHOT/parser-1.0.0-SNAPSHOT.jar --spring.profiles.active=production
WorkingDirectory=/opt/toop/telemetry/parser-1.0.0-SNAPSHOT
SuccessExitStatus=143
Restart=on-failure
RestartSec=20
[Install]
WantedBy=multi-user.target
```
报错
```
 parser-1.0.0-SNAPSHOT.jar: Unable to find Java
```

**Solution**
ln -s  /opt/nsp/os/jre/bin/java /sbin/java