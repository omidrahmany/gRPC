package io.omid.grpc.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "blog")
@Data
public class Blog {
    @Id
    private String id;
    private String authorId;
    private String title;
    private String content;
}
