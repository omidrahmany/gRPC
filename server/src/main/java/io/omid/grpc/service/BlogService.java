package io.omid.grpc.service;

import io.grpc.stub.StreamObserver;
import io.omid.grpc.blog.BlogInputDTO;
import io.omid.grpc.blog.BlogOutputDTO;
import io.omid.grpc.blog.BlogServiceGrpc;
import io.omid.grpc.model.Blog;
import io.omid.grpc.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class BlogService extends BlogServiceGrpc.BlogServiceImplBase {

    private final BlogRepository repository;
    private static BlogService blogService;

    @PostConstruct
    public void init() {
        blogService = this;
    }

    public static BlogService INSTANCE() {
        return blogService;
    }

    @Override
    public void createBlog(BlogInputDTO request, StreamObserver<BlogOutputDTO> responseObserver) {
        Blog blog = new Blog();
        blog.setAuthorId(request.getAuthorId());
        blog.setContent(request.getContent());
        blog.setTitle(request.getTitle());
        repository.save(blog);
        responseObserver.onNext(BlogOutputDTO.newBuilder().setResult("________________________> Successful!").build());
        responseObserver.onCompleted();
    }
}
