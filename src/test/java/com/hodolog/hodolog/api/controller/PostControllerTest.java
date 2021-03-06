package com.hodolog.hodolog.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hodolog.hodolog.api.domain.Post;
import com.hodolog.hodolog.api.repository.PostRepository;
import com.hodolog.hodolog.api.request.PostCreate;
import com.hodolog.hodolog.api.request.PostEdit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest // mockTest가 안된다.
// @WebMvcTest // 웹 레이어 테스트 - 간단한 웹 계층 테스트
@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void clean() {
        postRepository.deleteAll();
    }

    /*@Test
    @DisplayName("/posts 요청시 Hello World를 출력한다.")
    void test() throws Exception {

        // expected
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "글제목입니다.")
                        .param("content", "글 내용입니다.")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"))
                .andDo(print());
    }*/

    @DisplayName("/posts 요청시 Hello World를 출력한다.")
    @Test
    void test() throws Exception {
        // given
        PostCreate postCreate = PostCreate.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .build();

        String json = objectMapper.writeValueAsString(postCreate);
        System.out.println(json);

        // expected
        mockMvc.perform(post("/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(print());
    }

    @DisplayName("/posts 요청시 title값은 필수다.")
    @Test
    void test2() throws Exception {

        // given
        /*PostCreate postCreate = PostCreate.builder()
                .content("내용입니다.")
                .build();

        String json = objectMapper.writeValueAsString(postCreate);*/

        // expected
        /*mockMvc.perform(post("/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validation.title").value("타이틀을 입력해 주세요."))
                .andDo(print());*/
    }

    @DisplayName("/posts 요청시 DB에 값이 저장된다.")
    @Test
    void test3() throws Exception {
        // given
        PostCreate postCreate = PostCreate.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .build();

        String json = objectMapper.writeValueAsString(postCreate);

        // when
        mockMvc.perform(post("/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andDo(print());

        // then
        assertEquals(1L, postRepository.count());
        Post post = postRepository.findAll().get(0);
        assertEquals("제목입니다.", post.getTitle());
        assertEquals("내용입니다.", post.getContent());
    }

    @Test
    @DisplayName("글 1개 조회")
    void test4() throws Exception {
        //given
        Post post = Post.builder()
                .title("123456789012345")
                .content("bar")
                .build();
        postRepository.save(post);

        // 추가 요구사항(클라이언트 요구사항)
        // json응답에서 title값 길이를 최대 10글자로 해주세요.
        // Post entity <-> PostResponse는 같은 구조

        // expected = when + then
        mockMvc.perform(get("/posts/{postId}", post.getId())
                        .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value("1234567890"))
                .andExpect(jsonPath("$.content").value("bar"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 삭제")
    void test5() throws Exception {
        // given
        Post post = Post.builder()
                .title("foo")
                .content("bar")
                .build();
        postRepository.save(post);

        // expected when
        mockMvc.perform(delete("/posts/{postId}", post.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회")
    void test9() throws Exception {
        // expected when
        mockMvc.perform(delete("/posts/{postId}", 1L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정")
    void test10() throws Exception {

        // given
        PostEdit postEdit = PostEdit.builder()
                        .title("호돌걸")
                        .content("반포자이")
                        .build();

        // expected when
        mockMvc.perform(patch("/posts/{postId}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 작성시 바보가 들어가면 안된다.")
    void test11() throws Exception {

        // given
        PostCreate request = PostCreate.builder()
                .title("나는 바보입니다.")
                .content("반포자이")
                .build();

        String json = objectMapper.writeValueAsString(request);

        // expected when
        mockMvc.perform(post("/posts")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

// API 문서 생성 - swagger, io docs도 있음
// 클라이언트 입장에서는 어떤 API가 있는지 모름

// Spring RestDocs
// 장점 : 1. 운영중인 코드에 영향이 없다.
//    2. 어떤 docs는 코드 수정 -> 문서를 수정 안한다 => 시간이 흐르면 코드와 문서가 다르게 됨
// spring restdocs는 테스트 케이스를 만들어서 실행 => 통과되면 => 문서 생성되기 때문에 문서가 최신으로 유지 된다.

}