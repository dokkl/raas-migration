package com.sigudong.raas;

import com.sigudong.raas.model.CodeB;
import com.sigudong.raas.model.GisBjd;
import com.sigudong.raas.repository.CodeBRepository;
import com.sigudong.raas.repository.GisBjdRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;

@Slf4j
@SpringBootApplication
public class RaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(RaasApplication.class, args);
    }

    /**
     * https://www.code.go.kr/stdcode/regCodeL.do 에서 다운받은
     * 법정동 txt 파일에서 존재하는 정보중 읍면동 (3레벨) 까지있는것만 local mongodb 에 저장한다.
     */
    @Component
    public class SaveCodeB {
        @Bean
        CommandLineRunner init(CodeBRepository repository) {
            return args -> {
                log.info("ReadB start");
                try{
                    //파일 객체 생성
                    File file = new File("/Users/coupang/dev/workspace/raas/src/main/resources/bCode.txt");
                    //입력 스트림 생성
                    FileReader filereader = new FileReader(file);
                    //입력 버퍼 생성
                    BufferedReader bufReader = new BufferedReader(filereader);
                    String line = "";
                    int i = 1;
                    while((line = bufReader.readLine()) != null){
                        if (line.contains("존재")) {
                            String[] arrAddress = line.split("\\s+");
                            //log.info("arrAddress[0] : " + arrAddress[0]);
                            if (arrAddress[0].endsWith("00")) { //시도 시군구 읍면동 리  에서 읍면동(00) 레벨까지만
                                //log.info(line + " $$$$ ");
                                if (arrAddress.length > 4) {
                                    if ("000".equals(arrAddress[0].substring(5, 8))) {
                                        //log.info("000 => 제외처리 {} {} {} {} {}", arrAddress[0], arrAddress[1], arrAddress[2], arrAddress[3], arrAddress[4]);
                                        continue;
                                    }

                                    log.info(arrAddress[0] + " ## " + arrAddress[1] + " ## " + arrAddress[2] + " ## " + arrAddress[3] + " ## " + arrAddress[4] + "## " + i++);
                                    CodeB codeB = new CodeB();
                                    codeB.setCode(arrAddress[0]);
                                    codeB.setSido(arrAddress[1]);
                                    if ("존재".equals(arrAddress[4])) {
                                        codeB.setSigugun(arrAddress[2]);
                                        codeB.setEebmeundong(arrAddress[3]);
                                    } else {
                                        codeB.setSigugun(arrAddress[2] + " " + arrAddress[3]);
                                        codeB.setEebmeundong(arrAddress[4]);
                                    }
                                    codeB.setExist(true);

                                    repository.save(codeB).subscribe();
                                } else if (arrAddress.length == 4){  //example : 3611038000	세종특별자치시 전동면	존재
                                    if ("000".equals(arrAddress[0].substring(5, 8))) {
                                        //log.info("000 => 제외처리4 {} {} {} {} {}", arrAddress[0], arrAddress[1], arrAddress[2], arrAddress[3]);
                                        continue;
                                    }
                                    log.info(arrAddress[0] + " ## " + arrAddress[1] + " ## " + arrAddress[2] + " ## " + arrAddress[3] + "## " + i++);

                                    CodeB codeB = new CodeB();
                                    codeB.setCode(arrAddress[0]);
                                    codeB.setSido(arrAddress[1]);
                                    if ("존재".equals(arrAddress[3])) {
                                        codeB.setSigugun(arrAddress[1]);
                                        codeB.setEebmeundong(arrAddress[2]);
                                    }
                                    codeB.setExist(true);
                                    repository.save(codeB).subscribe();
                                }

                            }
                        }
                    }
                    //.readLine()은 끝에 개행문자를 읽지 않는다.
                    bufReader.close();
                }catch (FileNotFoundException fnfe) {
                    log.error("FileNotFoundException", fnfe);
                }catch(IOException ioe){
                    log.error("IOException", ioe);
                }

            };
        }
    }

    /**
     * 법정동 gis 정보 마이그레이션 (local -> dev or prod)
     */
    @Component
    public class GisBjdMigration {
        @Bean
        CommandLineRunner init(GisBjdRepository repository, CodeBRepository codeBRepository) {
            return args -> {
                log.info("GisBjdMigration called");
                //Flux<GisBjd> result = repository.findAll();
                Flux<GisBjd> result = repository.findAll().flatMap(data -> {
                    CodeB codeB = new CodeB();
                    codeB.setCode(data.getProperties().getEmdCd() + "00");
                    Example<CodeB> example = Example.of(codeB);
                    Mono<CodeB> codeBMono = codeBRepository.findOne(example);
                    return codeBMono
                            .defaultIfEmpty(new CodeB())
                            .map(codeB1 -> {
                                data.getProperties().setSido(codeB1.getSido());
                                data.getProperties().setSigugun(codeB1.getSigugun());
                                data.getProperties().setEebmeundong(codeB1.getEebmeundong());

                                if (codeB1.getCode() != null) {
                                    data.getProperties().setSidoCd(codeB1.getCode().substring(0, 2));
                                    data.getProperties().setSigugunCd(codeB1.getCode().substring(0, 5));
                                } else {
                                    log.info("data >> {} {}", data.getProperties().getEmdCd(), data.getProperties().getEmdKorNm());
                                }
                                return data;
                            });

                });

                //result.subscribe(cr -> log.info("Received {}", cr.toString()));

                //result.doOnNext(cr -> log.info("Received {}", cr.toString())).then();
                log.info("result.count().block() : " + result.count().block());

                WebClient.RequestHeadersSpec requestSpec1 = WebClient
                        .create("http://localhost:10001/api/sigudongb/migration")
                        .method(HttpMethod.POST)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(BodyInserters.fromPublisher(result, GisBjd.class));


                requestSpec1.exchange().doOnNext(cr -> log.info("Received {}", cr.toString())).block();

            };
        }
    }

    /**
     * 법정동코드 마이그레이션 (local -> dev or prod)
     */
    /*@Component
    public class CodeBMigration {
        @Bean
        CommandLineRunner codeBMigration(CodeBRepository codeBRepository) {
            return args -> {
                log.info("CodeMigrationB called");
                Flux<CodeB> result = codeBRepository.findAll();

                //result.subscribe(cr -> log.info("Received {}", cr.toString()));
                //result.doOnNext(cr -> log.info("Received {}", cr.toString())).then();
                //log.info("result.count().block() : " + result.count().block());

                WebClient.RequestHeadersSpec requestSpec1 = WebClient
                        .create("http://localhost:10001/api/codeb/migration")//raas dev zone
                        .method(HttpMethod.POST)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(BodyInserters.fromPublisher(result, CodeB.class));

                requestSpec1.exchange().doOnNext(cr -> log.info("Received {}", cr.toString())).block();

            };
        }
    }*/


    /**
     * gis 정보는 있지만 codeB (읍면동 코드) 에는 없는것 확인하는 로직
     */
    /*@Component
    public class CodeBForConfirm {
        @Bean
        CommandLineRunner init(GisBjdRepository repository, CodeBRepository codeBRepository) {
            log.info("init!!!!!");
            return args -> {
                Flux<GisBjd> gisBjdFlux = repository.findAll();
                Flux<String> emdCoFlux = gisBjdFlux.map(gisBjd -> gisBjd.getProperties().getEmdCd());
                Set<String> codeSet = emdCoFlux.collect(Collectors.toSet()).block();
                log.info("codeSet size : {}", codeSet.size());
                codeSet.stream().forEach(code -> log.info("code : {}", code));


                log.info("gisBjdFlux.count().block() : {}", gisBjdFlux.count().block());

                gisBjdFlux.doOnNext(gisBjd -> {
                    //log.info("doOnNext!!!! {}", gisBjd.getProperties().getEmdCd());
                    Mono<CodeB> codeBMono = codeBRepository.findByCode(gisBjd.getProperties().getEmdCd() + "00");
                    codeBMono.defaultIfEmpty(new CodeB("empty_" + gisBjd.getProperties().getEmdCd()))
                    .subscribe(codeB -> {
                        //log.info("codeB : {}", codeB.getEebmeundong());
                        if (codeB.getCode().startsWith("empty")) {
                            log.info("==> {} {}", gisBjd.getProperties().getEmdCd()
                                    , gisBjd.getProperties().getEmdKorNm());
                        }
                    });
                }).subscribe();

            };
        }
    }*/

    /**
     * 행정동 gis 마이그레이션
     */
    /*@Component
    public class SigudongMigrationH {
        @Bean
        CommandLineRunner init(SigudongRepository repository) {
            return args -> {
                Flux<Sigudong> result = repository.findAll().map(data -> {
                    String name[] = data.getProperties().getAdmNm().split(" ");
                    data.getProperties().setSi(name[0]);
                    data.getProperties().setGu(name[1]);
                    data.getProperties().setDong(name[2]);
                    return data;
                });

                WebClient.RequestHeadersSpec requestSpec1 = WebClient
                        .create("http://localhost:10001/api/sigudong/migration")
                        .method(HttpMethod.POST)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(BodyInserters.fromPublisher(result, Sigudong.class));


                requestSpec1.exchange().doOnNext(cr -> log.info("Received {}", cr.toString())).block();
            };
        }
    }*/
}
