package com.example.back;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.naming.NoPermissionException;
import javax.transaction.Transactional;

import com.example.back.controller.PostController;
import com.example.back.dto.PostDto.readPostDto;
import com.example.back.model.post.PostInformation;
import com.example.back.model.user.UserAuth;
import com.example.back.repository.PostInformationRepository;
import com.example.back.repository.UserAuthRepository;
import com.example.back.response.ResponseDto.CreateResponseDto;
import com.example.back.response.ResponseDto.ReadResponseDto;
import com.example.back.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import io.swagger.models.Response;



@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = PostController.class)
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
  
    private PostInformation testPostInfo_1 = new PostInformation();
    private PostInformation testPostInfo_2 = new PostInformation();
    private PostInformation testPostInfo_3 = new PostInformation();
    
    @MockBean
	private PostController postController;

    @MockBean
    private PostInformationRepository postInfoRepo;

    @MockBean
    private PostService postService;

    @MockBean    
    private UserAuthRepository userAuthRepository;

    String testUserToken;
    int postId;

    @BeforeEach
    @Test
    public void init(){
        System.out.println("hey");
        int price = 0;
        testPostInfo_1 = PostInformation.builder()
                        .title("?????????1")
                        .contents("testPostInfo_1??? ???????????? ??????????????????.")
                        .displayLevel("public")
                        .price(price)
                        .userId(22)
                        .build();

        testPostInfo_2 = PostInformation.builder()
                        .title("?????????2")
                        .contents("testPostInfo_2??? ???????????? ??????????????????.")
                        .displayLevel("private")
                        .price(price)
                        .userId(23)
                        .build();

        price = 500;
        testPostInfo_3 = PostInformation.builder()
                        .title("?????????3")
                        .contents("testPostInfo_3?????? ???????????? ??????????????????.")
                        .displayLevel("private")
                        .userId(2)
                        .build();

    }

    @Test
    public void getUserAuth(){
        System.out.println(testPostInfo_1.getUserId());
        UserAuth userAuth = userAuthRepository.findByUserId(testPostInfo_1.getUserId());
        String token = userAuth.getToken();
        System.out.println(token);
        
    }


    //???????????? ?????????
    @Test
    public void createPostControllerTest() throws Exception{
        
        // System.out.println(testPostInfo_1.getUserId());
        // UserAuth userAuth = userAuthRepository.findByUserId(testPostInfo_1.getUserId());
        // String token = userAuth.getToken();
        // System.out.println(token);
        
        String content = objectMapper.writeValueAsString(testPostInfo_1);
       
        // given (?????? ?????? ??????????????? ??????, ??????)
        ResponseEntity<CreateResponseDto> entity = new ResponseEntity<>(HttpStatus.CREATED);
        when(postController.createPost(any(), any())).thenReturn(entity);
        
        
        //when (??????)
        ResultActions resultActions = mockMvc.perform(post("http://localhost:8080/posts/writeRequest")
                                            .contentType("application/json")
                                            .content(content))
                                            .andDo(print());
        
        //??????
        resultActions
                    .andExpect(status().isCreated())
                    .andDo(print());


}

    @Test
    public void readPostControllerTest() throws Exception{

       ResponseEntity<ReadResponseDto> entity = new ResponseEntity<>(HttpStatus.OK);
       when(postController.readPost(any(), any())).thenReturn(entity);
    
       readPostDto rDto = new readPostDto(22, 1);
       String content = objectMapper.writeValueAsString(rDto);

       //when
       ResultActions resultActions = mockMvc.perform(post("http://localhost:8080/posts/readRequest")
                                            .contentType("application/json")
                                            .content(content))
                                            .andDo(print());
        
        resultActions
                    .andExpect(status().isOk())
                    .andDo(print());
        
        

    }




}
