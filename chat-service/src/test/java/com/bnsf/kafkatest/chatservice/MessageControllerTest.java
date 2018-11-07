package com.bnsf.kafkatest.chatservice;

import com.bnsf.kafkatest.chatservice.beans.Message;
import com.bnsf.kafkatest.chatservice.controllers.MessageController;
import com.bnsf.kafkatest.chatservice.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.print.attribute.standard.Media;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@WebMvcTest(value= MessageController.class, secure = false)
public class MessageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MessageService messageService;

	@Test
	public void testSendToAllSuccess() throws Exception{
		Response response = new Response(true, "Hello");
		String responseString = this.convertToJson(response);
		String uri = "/MC/send";
		Mockito.when(messageService.sendMessage(Mockito.any(Message.class))).thenReturn(response);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON);
		MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse httpResponse = mvcResult.getResponse();
		assertThat(responseString).isEqualTo(httpResponse.getContentAsString());
		assertThat("200").isEqualTo(String.valueOf(httpResponse.getStatus()));
	}

	@Test
	public void testSendToAllFailure() throws Exception{
		Response response = new Response(false, null);
		String responseString = this.convertToJson(response);
		String uri = "/MC/send";
		Mockito.when(messageService.sendMessage(Mockito.any(Message.class))).thenReturn(response);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON);
		MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse httpResponse = mvcResult.getResponse();
		assertThat(responseString).isEqualTo(httpResponse.getContentAsString());
		assertThat("500").isEqualTo(String.valueOf(httpResponse.getStatus()));
	}



	private String convertToJson(Object input) throws JsonProcessingException{
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(input);
	}
}
