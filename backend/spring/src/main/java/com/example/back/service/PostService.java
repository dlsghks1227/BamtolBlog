package com.example.back.service;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.NoPermissionException;

import com.example.back.config.CustomModelMapper;
import com.example.back.dto.PostDto.CreatePostDto;
import com.example.back.dto.PostDto.UpdatePostDto;
import com.example.back.exception.ErrorCode;
import com.example.back.model.post.PostInformation;
import com.example.back.model.post.Posts;
import com.example.back.model.user.Users;
import com.example.back.repository.PermissionRepository;
import com.example.back.repository.PostInformationRepository;
import com.example.back.repository.PostPermissionRepository;
import com.example.back.repository.PostsRepository;
import com.example.back.repository.SubscribePostRepository;
import com.example.back.repository.SubscribeUserRepository;
import com.example.back.repository.UserInformationRepository;
import com.example.back.repository.UserPermissionReposotiry;
import com.example.back.repository.UserRepository;
import com.example.back.response.ResponseDto.CreateResponseDto;
import com.example.back.response.ResponseDto.DeleteResponseDto;
import com.example.back.response.ResponseDto.ReadResponseDto;
import com.example.back.response.ResponseDto.UpdateResponseDto;
import com.example.back.security.JwtProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;


@Service
public class PostService {

    @Autowired  
    PostInformationRepository postInformationRepository;

    @Autowired
    PostsRepository postsRepository;

    @Autowired
    UserRepository urRepo;

    @Autowired
    UserInformationRepository urInfoRepo;

    @Autowired
    UserPermissionReposotiry urPermitRepo;

    @Autowired
    PostPermissionRepository postPermissionRepo;

    @Autowired
    SubscribeUserRepository subscribeUserRepository;

    @Autowired
    SubscribePostRepository subscribePostRepository;

    @Autowired
    CustomModelMapper customModelMapper;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    ManageAllAboutRole roleProcessor;

    @Autowired
    JwtProvider jwtProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(PostService.class);

    //private PostInfoMapper postInfoMapper


    // TODO : DTO?????? userId??? ??????, 

    private int getUserIdFromJWT(String token){

        return jwtProvider.getUserIdFromJWT(token);
    }

    private String getTokenFronCookie(HttpHeaders headers){
        
        List<String> extractToken = jwtProvider.resolveToken(headers);
        
        if(extractToken.isEmpty())
            return null;

        String token = jwtProvider.parseJwtInsideCookie(extractToken.get(0));
        return token;
    }


    @Transactional
    public CreateResponseDto createPost(HttpHeaders header, CreatePostDto createPostInfo) throws SQLException, NoPermissionException{

        // throw??? ????????? ??? ?????? ????????? ??? ??????.
        // userId??? ?????? ??? ?????????, ????????? ?????? ?????????, ?????? ???????????? ?????? ID??????, ???????????? ??????????????? ?????? ??????

        String token = getTokenFronCookie(header);
        int userId = -1;
        
        if(token == null) {
            LOGGER.info("??????????????????.");
            throw new NoPermissionException(ErrorCode.PERMISSION_DENIED.getMessage());
        }
        else userId = getUserIdFromJWT(token);

       
        Users users = urRepo.findById(userId).orElseThrow(()->
            new NullPointerException("NICKNAME NULL")
        ); 
        
        int publisherUserId = users.getId();

        // member????????? ??????????????????.
        CreateResponseDto createDto = new CreateResponseDto();

        // ?????? ???????????? ??? ????????? ??????...
        // user??? ?????? ????????? ???, ????????? ??????????????? if??? ??? ????????? ??????...
        if(publisherUserId == userId){

            LOGGER.info("?????? ????????? ?????????????????????.");

            PostInformation newPostInfo = new PostInformation();
        
            newPostInfo.setUserId(users.getId());
            customModelMapper.strictMapper().map(createPostInfo, newPostInfo);
        
            newPostInfo.setId(null);

            int postId = postInformationRepository.savePostInformationAndReturnPostId(newPostInfo);
            roleProcessor.saveUserRole("PUBLISHER", users.getId(), postId);

            createDto.setStatus(201);
            createDto.setMessage("???????????? ??????????????? ?????????????????????.");
            createDto.setPostId(postId);
        }
        else{
            new NoPermissionException("PERMISSION DENIED");
        }

        return createDto;
    }

    // token?????? id??? ???????????? ????????? ?????? ??? ?????????
    // Unknown column 'subscribeu0_.publisher_id' in 'field list'
    public ReadResponseDto readPost(HttpHeaders header, int postId) throws NoPermissionException, InternalServerError, AccessDeniedException, NoSuchElementException{


        String token = getTokenFronCookie(header);
        int userId = -1;
        
        if(token == null) LOGGER.info("??????????????????.");
        else userId = getUserIdFromJWT(token);

       
        HashMap<String,String> roleMap = roleProcessor.readRole(postId, userId);

                
        boolean isProperAccess = roleProcessor.isProperAccessRequest(roleMap.get("userPermissionLevel"), roleMap.get("postPermissionLevel"));
        PostInformation postInfo = postInformationRepository.findByPostId(postId).orElseThrow(()->{
            throw new NoSuchElementException("POST NOT FOUND");
        });

        if(isProperAccess){
            return new ReadResponseDto(200, "?????? ????????? ?????????????????????.", postInfo.getContents(), postInfo.getTitle());
        }
        else{
            //false ??? ????????? ????????? ?????? ??????
            throw new NoPermissionException(ErrorCode.PERMISSION_DENIED.name());
        }
    
    }

    public UpdateResponseDto updatePost(HttpHeaders header, UpdatePostDto body, int postId) throws NoPermissionException, NoSuchElementException{

        String token = getTokenFronCookie(header);
        int userId = -1;
        
        if(token == null) {
            LOGGER.info("??????????????????.");
            throw new NoPermissionException(ErrorCode.PERMISSION_DENIED.getMessage());
        }
        else userId = getUserIdFromJWT(token);

        System.out.println("p :" + postId);

        Posts post = postsRepository.findById(postId).orElseThrow(()->
            new NoSuchElementException("POST NOT FOUND")
        );


        int publisherUserId = post.getUserId();

        UpdateResponseDto updateDto = new UpdateResponseDto();
        
        if(publisherUserId == userId){

            LOGGER.info("?????? ????????? ?????????????????????.");

            PostInformation postInfo = postInformationRepository.findByPostId(postId).get();
            
            postInfo.setTitle(body.getTitle());
            postInfo.setContents(body.getContents());
            postInfo.setDisplayLevel(body.getDisplayLevel());
            postInfo.setPrice(body.getPrice());

            //customModelMapper.strictMapper().map(body, postInfo);
            //postInfo = PostInfoMapper.INSTANCE.updateDtoToPostInfoEntity(body);
            postInformationRepository.save(postInfo);

            updateDto.setStatus(200);
            updateDto.setMessage("?????? ????????? ?????????????????????.");
            
        }
        else{
            throw new NoPermissionException(ErrorCode.PERMISSION_DENIED.name());
        }

        return updateDto;
    }
    

    public DeleteResponseDto deletePost(HttpHeaders header, int postId) throws NoPermissionException, NullPointerException, NoSuchElementException{

        String token = getTokenFronCookie(header);
        int userId = -1;
        
        if(token == null) {
            LOGGER.info("??????????????????.");
            throw new NoPermissionException(ErrorCode.PERMISSION_DENIED.getMessage());
        }
        else userId = getUserIdFromJWT(token);

        //  ?????? ???????????? ??????, ????????? ?????? NULL???????
        LOGGER.info("postID :" + postId);
        Posts post = postsRepository.findById(postId).orElseThrow(()->
            new NoSuchElementException("POST NOT FOUND")
        );

        int publisherUserId = post.getUserId();

        DeleteResponseDto deleteDto = new DeleteResponseDto();
        
        if(publisherUserId == userId){
            postsRepository.delete(post);
            deleteDto.setStatus(200);
            deleteDto.setMessage("?????? ????????? ?????????????????????.");
        }
        else{
             //false ??? ????????? ????????? ?????? ??????
            throw new NoPermissionException(ErrorCode.PERMISSION_DENIED.name());
        }

        return deleteDto;
    }   
    

}
