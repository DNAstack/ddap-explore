# DAM/IC Proto Files

We use protoc in our build to generate Java classes for these proto definitions.

We need to compile all the DAM/IC protos, even though we don't use the DAM in this app
because the ddap-common library depends on them as runtime provided classes.

If you remove the dam_proto, Spring Boot will fail to start with a `NoClassDefFoundError` because
of classes it scans in ddap-common.