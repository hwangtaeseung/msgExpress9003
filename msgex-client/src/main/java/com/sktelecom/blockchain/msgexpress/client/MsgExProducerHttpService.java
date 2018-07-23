package com.sktelecom.blockchain.msgexpress.client;

import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExMessageDto;
import com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExResponseDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExConst.MESSGAE_RECEIVE_URI_PARAM_FOR_RETROFIT;
import static com.sktelecom.blockchain.msgexpress.common.protocol.message.MsgExConst.PRODUCER_URI;

/**
 * Send Message To Producer Http Server
 */
public interface MsgExProducerHttpService {
    @POST(PRODUCER_URI + MESSGAE_RECEIVE_URI_PARAM_FOR_RETROFIT)
    Call<MsgExResponseDto> transferMessage(@Path("msgId") String msgId, @Body MsgExMessageDto message);
}
