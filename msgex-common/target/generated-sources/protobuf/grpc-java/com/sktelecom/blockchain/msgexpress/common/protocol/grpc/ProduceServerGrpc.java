package com.sktelecom.blockchain.msgexpress.common.protocol.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * producer
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.10.0)",
    comments = "Source: basicmessage.proto")
public final class ProduceServerGrpc {

  private ProduceServerGrpc() {}

  public static final String SERVICE_NAME = "ProduceServer";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getSendMessageMethod()} instead. 
  public static final io.grpc.MethodDescriptor<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest,
      com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse> METHOD_SEND_MESSAGE = getSendMessageMethodHelper();

  private static volatile io.grpc.MethodDescriptor<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest,
      com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse> getSendMessageMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest,
      com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse> getSendMessageMethod() {
    return getSendMessageMethodHelper();
  }

  private static io.grpc.MethodDescriptor<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest,
      com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse> getSendMessageMethodHelper() {
    io.grpc.MethodDescriptor<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest, com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse> getSendMessageMethod;
    if ((getSendMessageMethod = ProduceServerGrpc.getSendMessageMethod) == null) {
      synchronized (ProduceServerGrpc.class) {
        if ((getSendMessageMethod = ProduceServerGrpc.getSendMessageMethod) == null) {
          ProduceServerGrpc.getSendMessageMethod = getSendMessageMethod = 
              io.grpc.MethodDescriptor.<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest, com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ProduceServer", "sendMessage"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ProduceServerMethodDescriptorSupplier("sendMessage"))
                  .build();
          }
        }
     }
     return getSendMessageMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ProduceServerStub newStub(io.grpc.Channel channel) {
    return new ProduceServerStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ProduceServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ProduceServerBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ProduceServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ProduceServerFutureStub(channel);
  }

  /**
   * <pre>
   * producer
   * </pre>
   */
  public static abstract class ProduceServerImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * message 전송
     * </pre>
     */
    public void sendMessage(com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest request,
        io.grpc.stub.StreamObserver<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getSendMessageMethodHelper(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSendMessageMethodHelper(),
            asyncUnaryCall(
              new MethodHandlers<
                com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest,
                com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse>(
                  this, METHODID_SEND_MESSAGE)))
          .build();
    }
  }

  /**
   * <pre>
   * producer
   * </pre>
   */
  public static final class ProduceServerStub extends io.grpc.stub.AbstractStub<ProduceServerStub> {
    private ProduceServerStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ProduceServerStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProduceServerStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ProduceServerStub(channel, callOptions);
    }

    /**
     * <pre>
     * message 전송
     * </pre>
     */
    public void sendMessage(com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest request,
        io.grpc.stub.StreamObserver<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSendMessageMethodHelper(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * producer
   * </pre>
   */
  public static final class ProduceServerBlockingStub extends io.grpc.stub.AbstractStub<ProduceServerBlockingStub> {
    private ProduceServerBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ProduceServerBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProduceServerBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ProduceServerBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * message 전송
     * </pre>
     */
    public com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse sendMessage(com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest request) {
      return blockingUnaryCall(
          getChannel(), getSendMessageMethodHelper(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * producer
   * </pre>
   */
  public static final class ProduceServerFutureStub extends io.grpc.stub.AbstractStub<ProduceServerFutureStub> {
    private ProduceServerFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ProduceServerFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ProduceServerFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ProduceServerFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * message 전송
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse> sendMessage(
        com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSendMessageMethodHelper(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEND_MESSAGE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ProduceServerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ProduceServerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEND_MESSAGE:
          serviceImpl.sendMessage((com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageRequest) request,
              (io.grpc.stub.StreamObserver<com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.sendMessageResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ProduceServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ProduceServerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.sktelecom.blockchain.msgexpress.common.protocol.grpc.Basicmessage.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ProduceServer");
    }
  }

  private static final class ProduceServerFileDescriptorSupplier
      extends ProduceServerBaseDescriptorSupplier {
    ProduceServerFileDescriptorSupplier() {}
  }

  private static final class ProduceServerMethodDescriptorSupplier
      extends ProduceServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ProduceServerMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ProduceServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ProduceServerFileDescriptorSupplier())
              .addMethod(getSendMessageMethodHelper())
              .build();
        }
      }
    }
    return result;
  }
}
