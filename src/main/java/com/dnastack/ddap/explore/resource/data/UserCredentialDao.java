package com.dnastack.ddap.explore.resource.data;

import com.dnastack.ddap.explore.resource.model.UserCredential;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterBeanMappers;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterBeanMappers(
    value = @RegisterBeanMapper(UserCredential.class)
)
public interface UserCredentialDao {

    @Transaction
    @SqlUpdate("INSERT INTO " +
        "user_credentials(principal_id,interface_id,creation_time,expiration_time,encrypted_credentials) " +
        "values(:principalId,:interfaceId,:creationTime,:expirationTime,:encryptedCredentials)"
    )
    void createUserCredential(@BindBean UserCredential credential);

    @Transaction
    @SqlBatch("INSERT INTO " +
        "user_credentials(principal_id,interface_id,creation_time,expiration_time,encrypted_credentials) " +
        "values(:principalId,:interfaceId,:creationTime,:expirationTime,:encryptedCredentials)"
    )
    void createUserCredentials(@BindBean Collection<UserCredential> credential);

    @SqlQuery("SELECT * FROM user_credentials WHERE principal_id = :principalId")
    List<UserCredential> getUserCredentials(@Bind("principalId") String principalId);

    @SqlQuery("SELECT * FROM user_credentials WHERE principal_id = :principalId AND interface_id in (<interfaceIds>)")
    List<UserCredential> getCredentialsForResources(@Bind("principalId") String principalId, @BindList("interfaceIds") List<String> interfaceIds);

    @SqlQuery("SELECT * FROM user_credentials WHERE principal_id = :principalId AND interface_id = :interfaceId")
    Optional<UserCredential> getCredentialForResource(@Bind("principalId") String principalId, @Bind("interfaceId") String interfaceId);

    @Transaction
    @SqlUpdate("DELETE\n"
        + "from user_credentials u\n"
        + "WHERE u.expiration_time\n"
        + "    < NOW()\n"
        + "   OR u.principal_id in (\n"
        + "    SELECT uc.principal_id\n"
        + "    FROM user_credentials uc\n"
        + "             LEFT JOIN sessions s on s.principal_id = uc.principal_id\n"
        + "    WHERE s.principal_id IS NULL\n"
        + ")")
    int deleteExpiredCredentials();

    @Transaction
    @SqlUpdate("DELETE FROM user_credentials WHERE expiration_time < NOW()")
    void delete();

    @Transaction
    @SqlUpdate("DELETE FROM user_credentials WHERE principal_id = :principalId AND interface_id = :interfaceId")
    void deleteCredential(@Bind("principalId") String principalId, @Bind("interfaceId") String interfaceId);

    @Transaction
    @SqlBatch("DELETE FROM user_credentials WHERE principal_id = :principalId AND interface_id = :interfaceId")
    void deleteCredentials(@Bind("principalId") String principalId, @Bind("interfaceId") Collection<String> interfaceIds);

    @Transaction
    @SqlUpdate("DELETE FROM user_credentials WHERE principal_id = :principalId")
    void deleteUserCredentials(@Bind("principalId") String principalId);

}
