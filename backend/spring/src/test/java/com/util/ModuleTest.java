package com.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.back.MainApplication;
import com.example.back.config.PostInfoMapper;
import com.example.back.dto.PostDto;
import com.example.back.dto.PostDto.UpdatePostDto;
import com.example.back.model.post.PostInformation;
import com.example.back.model.post.Posts;
import com.example.back.model.user.Users;
import com.example.back.security.JwtProvider;
import com.example.back.service.PostService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;


@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
public class ModuleTest {
    

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtProvider.class);

    @MockBean
    WithMockCustomUser withUsers;
    
    @Autowired
    private JwtProvider jwtProvider;
    
    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;

    int testUserId;

    // token ??????
    private String token;

    // post update
    UpdatePostDto updatePostDto = new UpdatePostDto();
    Gson gson = new Gson();

    String deletedPost = "97";
    String ExistedPost = "102";


    @BeforeEach
    public void setUp(){

        Authentication authentication = new UsernamePasswordAuthenticationToken("1234@naver.com", "1234");

        SecurityContextHolder.getContext().setAuthentication(authentication);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

        // ?????? ?????? ?????? ?????? ??? ??????.
        //ReflectionTestUtils.setField(postService, "headers", new HttpHeaders());

        ReflectionTestUtils.setField(updatePostDto, "title", "updateTest");
        ReflectionTestUtils.setField(updatePostDto, "contents", "updateTesting~~");
        ReflectionTestUtils.setField(updatePostDto, "displayLevel", "public");
        ReflectionTestUtils.setField(updatePostDto, "price", 0);
        
    }


    @Test
    @WithAnonymousUser
    // ???????????? ?????? ????????? ???????????? ???????????????
    // ?????? ????????? ????????? ????????? ??????
    void unAuthorizeTest(){

        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

    }


    @Test
    @WithUserDetails
    void securityContextTest(){


    }

    @Test
    @WithMockCustomUser
    public void LoginSecurityTest() throws Exception{   
            
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String password = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
            
            // interface??? ????????? ??? ????????? ??????????!
            Jsonify tojson = new Jsonify(email, password);

            Gson gson = new Gson();
            String json =  gson.toJson(tojson);

            System.out.println("json :"+ json);

            
            ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(json));

            result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
                
            
            Cookie token = result.andReturn().getResponse().getCookie("access_Token");

            this.token = token.getValue();
        
        }

    @Test
    void extractUserIdFromJwt() throws Exception{
        this.LoginSecurityTest(); // token??? ?????????.

        boolean isValidated = jwtProvider.validateToken(this.token);

        if(isValidated){
            LOGGER.info("VALIDATE!");

            Integer userId = jwtProvider.getUserIdFromJWT(this.token);
            this.testUserId = userId;
        }
        else{
            LOGGER.info("not validated token");
        }

        // userId??? ???????????? readPost ?????? ??????

    }

    @Test
    void loginTest() throws Exception{

        JSONObject loginInfoJson = new JSONObject();

        loginInfoJson.put("email", "1234@naver.com");
        loginInfoJson.put("password", "1234");

        Jsonify tojson = new Jsonify("1234@naver.com", "1234");

        Gson gson = new Gson();
        String json =  gson.toJson(tojson);
        
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json));

        result
            .andDo(MockMvcResultHandlers.print());
            
    }


    // subuser ????????? publisher_id??? ?????? ??? ????????? ????????? ?????? -> flyway??? ????????? ??????...
    @Test
    void readPostTest() throws Exception{
        this.extractUserIdFromJwt();

        HttpHeaders httpHeaders = new HttpHeaders();
        
        httpHeaders.add(HttpHeaders.SET_COOKIE, "access_Token=" + this.token);
        LOGGER.info("token ?????? : "+ this.token);
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders
                                            .get("/posts/101")
                                            .headers(httpHeaders)
                                            );

        result
            .andDo(MockMvcResultHandlers.print())
            .andReturn().getResponse().getContentAsString();

    }

    @Test
    void updatePostTest() throws Exception{
        this.extractUserIdFromJwt();

        HttpHeaders httpHeaders = new HttpHeaders();
        
        httpHeaders.add(HttpHeaders.SET_COOKIE, "access_Token=" + this.token);
        LOGGER.info("token ?????? : "+ this.token);
        LOGGER.info("update Data toString" + updatePostDto.toString());

        String updateJson = gson.toJson(updatePostDto);

        // TODOS : ?????? ????????? ?????? ????????? ExceptionRespose??? ????????? Exception?????? ????????????. 
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders
                                            .put("/posts/" + deletedPost)
                                            .headers(httpHeaders)
                                            
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(updateJson)
                                            );
                
        // ????????? 200?????? ????????? ????????? ??? ????????????
        result
            .andDo(MockMvcResultHandlers.print())
            .andReturn().getResponse().getContentAsString();

    }

    @Test
    void DeletePostTest() throws Exception{
        this.extractUserIdFromJwt();

        HttpHeaders httpHeaders = new HttpHeaders();
        
        httpHeaders.add(HttpHeaders.SET_COOKIE, "access_Token=" + this.token);
        LOGGER.info("token ?????? : "+ this.token);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders
                                            .delete("/posts/" + ExistedPost)
                                            .headers(httpHeaders)
                                            );
                
        // ????????? 200?????? ????????? ????????? ??? ????????????
        result
            .andDo(MockMvcResultHandlers.print())
            .andReturn().getResponse().getContentAsString();

    }


    @Test
    void createPostTest() throws Exception{
        this.extractUserIdFromJwt();

        HttpHeaders httpHeaders = new HttpHeaders();
        
        httpHeaders.add(HttpHeaders.SET_COOKIE, "access_Token=" + this.token);
        LOGGER.info("token ?????? : "+ this.token);
        //LOGGER.info("update Data toString" + updatePostDto.toString());

        String updateJson = gson.toJson(updatePostDto);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders
                                            .post("/posts/write")
                                            .headers(httpHeaders)
                                            
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(updateJson)
                                            );
                
        // ????????? 200?????? ????????? ????????? ??? ????????????
        result
            .andDo(MockMvcResultHandlers.print())
            .andReturn().getResponse().getContentAsString();


    }

    @Test
    void mapperTest(){
        
        try{
            PostInformation postInfo = PostInfoMapper.INSTANCE.updateDtoToPostInfoEntity(this.updatePostDto);
            //assertEquals(postInfo.getContents(), "updateTesting~~");
        }
        catch(Exception e){
            LOGGER.debug(e.getMessage());
        }

    }

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void contextLoads() throws Exception {
        if (applicationContext != null) {
            String[] beans = applicationContext.getBeanDefinitionNames();

            for (String bean : beans) {
                System.out.println("bean : " + bean);
            }
        }
    }





}


