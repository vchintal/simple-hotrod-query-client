package org.everythingjboss.jdg;

import java.io.IOException;
import java.util.List;

import org.everythingjboss.jdg.domain.Person;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
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
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServers("127.0.0.1:11222").connectionPool().maxTotal(1)
				.marshaller(new ProtoStreamMarshaller());
		RemoteCacheManager cacheManager = new RemoteCacheManager(
				builder.build());
		RemoteCache<Long, Person> cache = cacheManager.getCache("default");

		SerializationContext ctx = ProtoStreamMarshaller
				.getSerializationContext(cacheManager);

		// generate the 'person.proto' schema file based on the annotations on
		// Person class and register it with the SerializationContext of the
		// client
		ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
		String personSchemaFile = protoSchemaBuilder.fileName("person.proto")
				.packageName("quickstart").addClass(Person.class).build(ctx);

		// register the schemas with the server too
		RemoteCache<String, String> metadataCache = cacheManager
				.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
		metadataCache.put("memo.proto", personSchemaFile);
		String errors = metadataCache
				.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
		if (errors != null) {
			throw new IllegalStateException(
					"Some Protobuf schema files contain errors:\n" + errors);
		}

		Person p1 = new Person(new Long(1), "Bill", "Clinton", 69);
		Person p2 = new Person(new Long(2), "Hillary", "Clinton", 67);

		cache.put(p1.getId(), p1);
		cache.put(p2.getId(), p2);

		QueryFactory<Query> qf = Search.getQueryFactory(cache);
		Query query = qf.from(Person.class).having("age").gt(68).toBuilder()
				.build();

		List<Person> persons = query.list();
		for (Person person : persons) {
			System.out.println(person);
		}
	}

}
