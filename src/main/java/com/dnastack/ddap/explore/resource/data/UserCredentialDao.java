package com.dnastack.ddap.explore.resource.data;

import com.dnastack.ddap.explore.resource.model.UserCredential;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterBeanMappers;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterBeanMappers(
    value = @RegisterBeanMapper(UserCredential.class)
)
public interface UserCredentialDao {

    @Transaction
    @SqlUpdate("INSERT INTO " +
        "user_credentials(principal_id,authorization_id,creation_time,expiration_time,token) " +
        "values(:principalId,:authorizationId,:creationTime,:expirationTime,:token)"
    )
    void createUserCredential(@BindBean UserCredential credential);

    @SqlQuery("SELECT * FROM user_credentials WHERE principal_id = :principalId")
    List<UserCredential> getUserCredentials(@Bind("principalId") String principalId);

    @SqlQuery("SELECT * FROM user_credentials WHERE principal_id = :principalId AND authorization_id in (<authorizationIds>)")
    List<UserCredential> getCredentialsForResources(@Bind("principalId") String principalId, @BindList("authorizationIds") List<String> resources);

    @SqlQuery("SELECT * FROM user_credentials WHERE principal_id = :principalId AND authorization_id = :authorizationId")
    Optional<UserCredential> getCredentialForResource(@Bind("principalId") String principalId, @Bind("authorizationId") String resources);

    @Transaction
    @SqlUpdate("DELETE FROM user_credentials WHERE expiration_time < NOW()")
    void deleteExpiredCredentials();

    @Transaction
    @SqlUpdate("DELETE FROM user_credentials WHERE principal_id = :principalId AND authorization_id = :authorizationId")
    void deleteCredential(@Bind("principalId") String principalId, @Bind("authorizationId") String authorizationId);


    @Transaction
    @SqlUpdate("DELETE FROM user_credentials WHERE principal_id = :principalId")
    void deleteUserCredentials(@Bind("principalId") String principalId);

}
