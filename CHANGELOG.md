## [1.3.0](https://github.com/joutio/joutio-notification/compare/v1.2.0...v1.3.0) (2026-11-23)

### Features

* notify challenge acceptance on room creation ([feb6b54](https://github.com/joutio/joutio-notification/commit/feb6b544f08f761bc396d4a3a084766b4db02e19))

## [1.2.0](https://github.com/joutio/joutio-notification/compare/v1.1.7...v1.2.0) (2026-11-20)

### Features

* **notification:** fresh challenge invitation on reminder ([6529cfb](https://github.com/joutio/joutio-notification/commit/6529cfb4250ae2934ebb4f3c0532a0e3229a42f0))

## [1.1.7](https://github.com/joutio/joutio-notification/compare/v1.1.6...v1.1.7) (2026-11-20)

### Bug Fixes

* **deps:** joutio-parent 2.4.1 with patched dependencies ([b215f71](https://github.com/joutio/joutio-notification/commit/b215f716675344318497322e0d1f12343186d110))

## [1.1.6](https://github.com/joutio/joutio-notification/compare/v1.1.5...v1.1.6) (2026-11-20)

### Performance Improvements

* **docker:** JDK 25 AOT cache at startup ([ad62ca9](https://github.com/joutio/joutio-notification/commit/ad62ca92c34b3a528916dd0e07b68570db8e1cd1))

## [1.1.5](https://github.com/joutio/joutio-notification/compare/v1.1.4...v1.1.5) (2026-11-20)

### Bug Fixes

* **docker:** rebuild on patched base images ([ca4db07](https://github.com/joutio/joutio-notification/commit/ca4db076c3acf4d11393975ba12065040f60dfe6))

## [1.1.4](https://github.com/joutio/joutio-notification/compare/v1.1.3...v1.1.4) (2026-11-20)

### Bug Fixes

* **deps:** joutio-parent 2.4.0, instance-scoped groups on one Kafka consumer per pod ([7a0e2f3](https://github.com/joutio/joutio-notification/commit/7a0e2f32d31d86dcb191a0e004625ece66d04669))

## [1.1.3](https://github.com/joutio/joutio-notification/compare/v1.1.2...v1.1.3) (2026-11-20)

### Bug Fixes

* **deps:** joutio-parent 2.3.0, readiness waits for instance-scoped processors ([b87bc9e](https://github.com/joutio/joutio-notification/commit/b87bc9e77cb99209169b251cad3fcb34312720f0))

## [1.1.2](https://github.com/joutio/joutio-notification/compare/v1.1.1...v1.1.2) (2026-11-20)

### Bug Fixes

* **deps:** joutio-parent 2.2.1, distinct Kafka consumer metrics ([d5246ea](https://github.com/joutio/joutio-notification/commit/d5246ea2021ba442ace2ed3bb515dc03a0a7cec1))

## [1.1.1](https://github.com/joutio/joutio-notification/compare/v1.1.0...v1.1.1) (2026-11-20)

### Bug Fixes

* **deps:** joutio-parent 2.2.0, kafka consumer, segment and rebalancer metrics ([4d7fec3](https://github.com/joutio/joutio-notification/commit/4d7fec30a8a509870f7e6a5580efa8c8c999c5e2))

## [1.1.0](https://github.com/joutio/joutio-notification/compare/v1.0.7...v1.1.0) (2026-11-19)

### Features

* **preferences:** REMATCH notification category ([98666a9](https://github.com/joutio/joutio-notification/commit/98666a91bc7e3d021042f6a1059a6b11e70ceb95))

## [1.0.7](https://github.com/joutio/joutio-notification/compare/v1.0.6...v1.0.7) (2026-11-19)

### Bug Fixes

* **deps:** joutio-parent 2.1.0, consistent command routing across replicas ([f0b04bf](https://github.com/joutio/joutio-notification/commit/f0b04bf02afb12c369db38898178e8e2f79d4f3d))

## [1.0.6](https://github.com/joutio/joutio-notification/compare/v1.0.5...v1.0.6) (2026-11-18)

### Bug Fixes

* **deps:** joutio-parent 2.0.0, idle-friendly processors and bytea Axon schema ([fde8d12](https://github.com/joutio/joutio-notification/commit/fde8d12cd20d5340399320dbedf7d462a20aeace))

## [1.0.5](https://github.com/joutio/joutio-notification/compare/v1.0.4...v1.0.5) (2026-11-17)

### Bug Fixes

* **deps:** pin joutio-theme-domain 3.8.3, 3.6.0 is no longer published ([73c9c96](https://github.com/joutio/joutio-notification/commit/73c9c96df55df51a5368944edcb2bfa36223a5fb))

## [1.0.4](https://github.com/joutio/joutio-notification/compare/v1.0.3...v1.0.4) (2026-11-17)

### Bug Fixes

* **deps:** joutio-parent 1.2.0, segmented processing groups and single-partition Axon topic ([46a644b](https://github.com/joutio/joutio-notification/commit/46a644bc26f8f0595243bd695b587e4eee531c18))

## [1.0.3](https://github.com/joutio/joutio-notification/compare/v1.0.2...v1.0.3) (2026-11-17)

### Bug Fixes

* **deps:** joutio-parent 1.1.0, unknown enum constants tolerated on the Axon transport ([971d451](https://github.com/joutio/joutio-notification/commit/971d451055c44ef1fecdd826a0b6b0af4069681c))

## [1.0.2](https://github.com/joutio/joutio-notification/compare/v1.0.1...v1.0.2) (2026-11-15)

### Bug Fixes

* **deps:** bump joutio-theme-domain to 3.6.0 ([c9de264](https://github.com/joutio/joutio-notification/commit/c9de264b4dd86a863f37d92e1245b71006a995f1))

## [1.0.1](https://github.com/joutio/joutio-notification/compare/v1.0.0...v1.0.1) (2026-11-10)

### Bug Fixes

* **notification:** keep question reports anonymous ([fbf06d5](https://github.com/joutio/joutio-notification/commit/fbf06d51266d7c94f9ceeca26b91e9f427fba61e))

## 1.0.0 (2026-10-27)

### Features

* release Joutio notification 1.0.0 ([d9c7152](https://github.com/joutio/joutio-notification/commit/d9c71522002a3bd855d03453a475d4d802fbe962))
* release Joutio notification 1.0.0 ([85c9b7f](https://github.com/joutio/joutio-notification/commit/85c9b7fd68082147ae606e1307e66949fbcb98a2))

## [3.10.0](https://github.com/joutio/joutio-notification/compare/v3.9.0...v3.10.0) (2026-10-22)

### Features

* **notification:** consomme l'événement de dépublication depuis le contrat theme ([23d32ca](https://github.com/joutio/joutio-notification/commit/23d32ca9ce8b5e7fe22b876fef46bea5f1264f98))

## [3.9.0](https://github.com/joutio/joutio-notification/compare/v3.8.1...v3.9.0) (2026-10-22)

### Features

* **account:** efface l'inbox et notifie les sujets repassés en brouillon ([73c5621](https://github.com/joutio/joutio-notification/commit/73c562101d5c0383ea3bcaeecf37a6e2d6cefc23))

## [3.8.1](https://github.com/joutio/joutio-notification/compare/v3.8.0...v3.8.1) (2026-10-18)

### Bug Fixes

* **notification:** consume identity suspension reasons 2.9.0 ([ce9bc89](https://github.com/joutio/joutio-notification/commit/ce9bc894418088906e28ab798f72641d0c715933))

## [3.8.0](https://github.com/joutio/joutio-notification/compare/v3.7.0...v3.8.0) (2026-10-18)

### Features

* **notification:** notify account suspension ([a3f0426](https://github.com/joutio/joutio-notification/commit/a3f04261d519b25258c2d953c4ea59cf293ac3da))

## [3.7.0](https://github.com/joutio/joutio-notification/compare/v3.6.0...v3.7.0) (2026-10-17)

### Features

* **notification:** notifie les signalements de questions ([d8fe978](https://github.com/joutio/joutio-notification/commit/d8fe9789e71d473dcd910016936aafe09e415f38))

## [3.6.0](https://github.com/joutio/joutio-notification/compare/v3.5.2...v3.6.0) (2026-10-16)

### Features

* **notification:** relance wizz en inbox unique par salle ([0e2af82](https://github.com/joutio/joutio-notification/commit/0e2af82a434c1b847dc78ab8a5ea3f65f9dce356))

## [3.5.2](https://github.com/joutio/joutio-notification/compare/v3.5.1...v3.5.2) (2026-10-12)

### Bug Fixes

* **notification:** SDK joutio-parent 4.7.1 (cycle de beans Configuration/bus distribué au démarrage) ([1baf276](https://github.com/joutio/joutio-notification/commit/1baf2767b3244b109f73107c52820eb80f166e68))

## [3.5.1](https://github.com/joutio/joutio-notification/compare/v3.5.0...v3.5.1) (2026-10-12)

### Bug Fixes

* **notification:** contrats matchmaking 10.2.0 / social 5.2.0 (élagués) ([730a2e2](https://github.com/joutio/joutio-notification/commit/730a2e2e32cb19b7472ba5e0bbc9c81f9fa93267))

## [3.5.0](https://github.com/joutio/joutio-notification/compare/v3.4.0...v3.5.0) (2026-10-12)

### Features

* **console:** consume joutio-sdk 4.7.0 (lag, commandes en vol, snapshots, scheduled) ([84cdee7](https://github.com/joutio/joutio-notification/commit/84cdee7abfc0baadc7003ba047d2b5a0839d9a49))

## [3.4.0](https://github.com/joutio/joutio-notification/compare/v3.3.0...v3.4.0) (2026-10-12)

### Features

* **console:** consume joutio-sdk 4.6.0 (build-info + API interne event store/scheduled) ([02db4aa](https://github.com/joutio/joutio-notification/commit/02db4aa68d2f57306fe25ad4e049c6516a9c6109))

## [3.3.0](https://github.com/joutio/joutio-notification/compare/v3.2.0...v3.3.0) (2026-10-12)

### Features

* **console:** consume joutio-sdk 4.5.2 (DLQ Axon par défaut + agent console) ([9e52082](https://github.com/joutio/joutio-notification/commit/9e52082414160a0a161fb41c9b9e8c6dc73221bf))

## [3.2.0](https://github.com/joutio/joutio-notification/compare/v3.1.0...v3.2.0) (2026-10-11)

### Features

* **notification:** l'annulation d'un défi supprime l'invitation (push temps réel) ([0d25648](https://github.com/joutio/joutio-notification/commit/0d256480846322179cf2bbb850209bc0c43a33e9))

## [3.1.0](https://github.com/joutio/joutio-notification/compare/v3.0.1...v3.1.0) (2026-10-11)

### Features

* **notification:** l'invitation de revanche porte le gameId d'origine ([68130c6](https://github.com/joutio/joutio-notification/commit/68130c663590e603313ac1cc849ffd1da16ecfdd))

## [3.0.1](https://github.com/joutio/joutio-notification/compare/v3.0.0...v3.0.1) (2026-10-11)

### Bug Fixes

* **notification:** aligne l'ingestion sur les contrats matchmaking 7.0.0 ([8d196fe](https://github.com/joutio/joutio-notification/commit/8d196fe5318e0e2c7a2c8f4e782aa463346ebdaf))

## [3.0.0](https://github.com/joutio/joutio-notification/compare/v2.7.0...v3.0.0) (2026-10-10)

### ⚠ BREAKING CHANGES

* **notification:** types/catégories de notification et schéma V1 réécrits.

### Features

* **notification:** types ROOM_*, routage supprimé, ingestion allégée ([787605d](https://github.com/joutio/joutio-notification/commit/787605df1451d2e61f6feddec0f5a77147f7a852))

## [2.7.0](https://github.com/joutio/joutio-notification/compare/v2.6.0...v2.7.0) (2026-10-10)

### Features

* **notification:** marquer une notification en non lue ([15e147c](https://github.com/joutio/joutio-notification/commit/15e147ca810be8fe85a47d9db699e0d680a15fbd))

## [2.6.0](https://github.com/joutio/joutio-notification/compare/v2.5.0...v2.6.0) (2026-10-08)

### Features

* **notification:** expire les invitations a la purge d'un defi ([bc5ee60](https://github.com/joutio/joutio-notification/commit/bc5ee60c4398f48d3daf8ddbda85c7ea01ebc532))

## [2.5.0](https://github.com/joutio/joutio-notification/compare/v2.4.0...v2.5.0) (2026-10-05)

### Features

* **notification:** suppression en masse de l'inbox (DeleteAllNotificationsCommand) ([a126098](https://github.com/joutio/joutio-notification/commit/a1260989e5845e9e0671d0f50016d6389ce2a142))

## [2.4.0](https://github.com/joutio/joutio-notification/compare/v2.3.0...v2.4.0) (2026-10-05)

### Features

* **notification:** ingestion du defi nominatif (CHALLENGE_RECEIVED/DECLINED) ([a4d40d8](https://github.com/joutio/joutio-notification/commit/a4d40d81c7c4c73a3b8533b69fb95056304f1b14))

## [2.3.0](https://github.com/joutio/joutio-notification/compare/v2.2.2...v2.3.0) (2026-10-05)

### Features

* **notification:** notification durable LOBBY_MISSED ([8d0ae83](https://github.com/joutio/joutio-notification/commit/8d0ae8364db4a42988d5edd617b82669167e0247))

## [2.2.2](https://github.com/joutio/joutio-notification/compare/v2.2.1...v2.2.2) (2026-10-05)

### Bug Fixes

* **notification:** rattache le gameId aux notifications d'acceptation ([fbef047](https://github.com/joutio/joutio-notification/commit/fbef04741f779ff343a0cdb911168e27c7c064ee))

## [2.2.1](https://github.com/joutio/joutio-notification/compare/v2.2.0...v2.2.1) (2026-10-05)

### Bug Fixes

* **notification:** ingestion idempotente face au replay apres suppression ([cef4a96](https://github.com/joutio/joutio-notification/commit/cef4a960ed0742c5d15e8d366e28552f6170711d))

## [2.2.0](https://github.com/joutio/joutio-notification/compare/v2.1.0...v2.2.0) (2026-10-04)

### Features

* **notification:** expiration des invitations closes et suppression des notifications d'annulation ([95846a7](https://github.com/joutio/joutio-notification/commit/95846a73c4e0f45719790ee12805d1598427a11f))

## [2.1.0](https://github.com/joutio/joutio-notification/compare/v2.0.0...v2.1.0) (2026-10-04)

### Features

* **notification:** suppression des notifications par le destinataire (hard delete) ([ad3828e](https://github.com/joutio/joutio-notification/commit/ad3828edcb23d3cee4a6397d4a9304293803ff85))

## [2.0.0](https://github.com/joutio/joutio-notification/compare/v1.0.2...v2.0.0) (2026-10-03)

### ⚠ BREAKING CHANGES

* **notification:** drop matchmaking notifications

### Features

* **notification:** drop matchmaking notifications ([435e7a1](https://github.com/joutio/joutio-notification/commit/435e7a1917fb52bab5f9cdb52868446ca4e95591))

## [1.0.2](https://github.com/joutio/joutio-notification/compare/v1.0.1...v1.0.2) (2026-10-03)

### Bug Fixes

* **notification:** notify invitee when a nominative lobby expires ([6581d6e](https://github.com/joutio/joutio-notification/commit/6581d6e8c9a64db92bc64ce74bb8a18045262a8d))

## [1.0.1](https://github.com/joutio/joutio-notification/compare/v1.0.0...v1.0.1) (2026-10-03)

### Bug Fixes

* **notification:** pin matchmaking-domain 4.1.0 ([e9c07f7](https://github.com/joutio/joutio-notification/commit/e9c07f7a138a2b22a89c12e872fca9da5661170d))

## 1.0.0 (2026-10-03)

### Features

* **notification:** add player notification service ([4677230](https://github.com/joutio/joutio-notification/commit/467723076c5002a9905618fb8a859d38c3a923a1))
