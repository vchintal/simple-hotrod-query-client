package org.everythingjboss.jdg;

import java.io.IOException;
import java.util.List;

import org.everythingjboss.jdg.domain.Person;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.protostream.annotations.ProtoSchemaBuilderException;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

public class SimpleHotRodQueryClient {

    public static void main(String[] args) throws ProtoSchemaBuilderException,
        IOException {
        
        // Build the cache configuration and instantiate a remote cache  
        Configuration remoteConfig = new ConfigurationBuilder()
                .addServers("127.0.0.1:11222")
                .marshaller(new ProtoStreamMarshaller())
                .build();
        RemoteCacheManager remoteCacheManager = new RemoteCacheManager(remoteConfig);
        RemoteCache<Long, Person> cache = remoteCacheManager.getCache("default");

        // Set ProtoStreamMarshaller as the default marshaller for serialization
        SerializationContext ctx = ProtoStreamMarshaller
                .getSerializationContext(remoteCacheManager);

        // Generate the 'person.proto' schema file based on the annotations on
        // Person class and register it with the SerializationContext of the
        // client
        ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
        String personSchemaFile = protoSchemaBuilder.fileName("person.proto")
                .packageName("org.everythingjboss.jdg.domain")
                .addClass(Person.class)
                .build(ctx);

        // Register the schemas with the JDG server by placing the schema into a 
        // special cache ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME
        RemoteCache<String, String> metadataCache = remoteCacheManager
                .getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
        metadataCache.put("person.proto", personSchemaFile);
        String errors = metadataCache
                .get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
        if (errors != null) {
            throw new IllegalStateException("Protobuf schema file contain errors:\n" + errors);
        }

        // Generate entries and place them in the remote cache
        Person p1 = new Person(new Long(1), "Bill", "Clinton", 69);
        Person p2 = new Person(new Long(2), "Hillary", "Clinton", 67);
        cache.put(p1.getId(), p1);
        cache.put(p2.getId(), p2);

        // Query for the remote cache for Person instances where age > 68
        QueryFactory qf = Search.getQueryFactory(cache);
        Query query = qf.from(Person.class)
                .having("age")
                .gt(68)
                .toBuilder()
                .build();
        List<Person> persons = query.list();
        persons.stream().forEach(System.out::println);
    }
}