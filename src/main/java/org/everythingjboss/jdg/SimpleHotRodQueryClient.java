package org.everythingjboss.jdg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	public static final Logger logger = LogManager.getLogger(SimpleHotRodQueryClient.class);

	public static void main(String[] args) throws ProtoSchemaBuilderException, IOException {

		Properties props = System.getProperties();

		Boolean skipInserts = props.containsKey("skipInserts");
		Boolean skipProtobuf = props.containsKey("skipProtobuf");

		logger.info("skipInserts="+skipInserts);
		logger.info("skipProtobuf="+skipProtobuf);

		// Build the cache configuration and instantiate a remote cache
		Configuration remoteConfig = new ConfigurationBuilder().addServers("127.0.0.1:11222")
				.marshaller(new ProtoStreamMarshaller()).build();
		RemoteCacheManager remoteCacheManager = new RemoteCacheManager(remoteConfig);
		RemoteCache<Long, Person> cache = remoteCacheManager.getCache("indexedCache");

		// Set ProtoStreamMarshaller as the default marshaller for serialization
		SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(remoteCacheManager);

		// Generate the 'person.proto' schema file based on the annotations on
		// Person class and register it with the SerializationContext of the
		// client
		ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
		String personSchemaFile = protoSchemaBuilder.fileName("person.proto")
				.packageName("org.everythingjboss.jdg.domain").addClass(Person.class).build(ctx);

		if (!skipProtobuf) {
			// Register the schemas with the JDG server by placing the schema
			// into a
			// special cache
			// ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME
			RemoteCache<String, String> metadataCache = remoteCacheManager
					.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
			metadataCache.put("person.proto", personSchemaFile);
			String errors = metadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
			if (errors != null) {
				throw new IllegalStateException("Protobuf schema file contain errors:\n" + errors);
			}
		} else logger.info("Skipping Protobuf publishing");

		if (!skipInserts) {
			logger.info("Putting entries into the cache");

			IntStream.rangeClosed(1, 100).forEach(i -> {
				Person p = new Person(new Long(i), "fn" + i, "ln" + i, new ArrayList<String>(), 15 + i);
				cache.put(p.getId(), p);
			});

			logger.info("Done putting entries");
		} else logger.info("Skipping priming the cache with sample dataset");

		// Query for the remote cache for Person instances where age > 68
		QueryFactory qf = Search.getQueryFactory(cache);
		Query query = qf.from(Person.class).having("lastName").like("%3%").build();

		List<Person> persons = query.list();
		persons.stream().forEach(System.out::println);
		
		remoteCacheManager.stop();
	}
}