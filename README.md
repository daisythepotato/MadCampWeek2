# AuctionKingdom

## Outline

---

![logo.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/f52d9a6d-7b6a-43dc-acfe-e6b7ae56b3bd/logo.png)

YaOng 2: AuctionKingdom은 2인이서 플레이할 수 있는 전략 게임입니다. 자산을 잘 분배해서 고양이 왕국의 힘을 키워보세요!

**Tech Stack**

- Front-End : Kotlin
- Server : node.js, express.js
- DB : mongoDB
- IDE : Android Studio, Visual Studio Code

## Team

---

[조성제](https://www.notion.so/544fcc087dd04ec0b5125dc16c3cab03?pvs=21) 

[choseongje - Overview](https://github.com/choseongje)

[🌲김해담솔](https://www.notion.so/36b8f992a3f3484a9eee2696e25bbdb5?pvs=21)

[daisythepotato - Overview](https://github.com/daisythepotato)

## Details

---

> **Intro**
> 
- Splash 화면을 통해 도장이 찍히는 애니메이션을 구현하여 게임의 핵심 키워드인 ‘Auction’을 강조했습니다.

![splash.gif](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/7956c6de-1c6a-4056-8985-82eb909538b3/splash.gif)

> **Login**
> 
- Google 로그인과 Guest 로그인을 구현했습니다.

![로그인 화면.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/0408e358-9893-4524-a16a-b32881d67617/%E1%84%85%E1%85%A9%E1%84%80%E1%85%B3%E1%84%8B%E1%85%B5%E1%86%AB_%E1%84%92%E1%85%AA%E1%84%86%E1%85%A7%E1%86%AB.png)

- Google로그인을 통해 처음 로그인을 하면 유저의 왕국 이름과 왕 이름을 설정하고, 원하는 고양이 왕의 프로필을 선택할 수 있습니다.

![프로필 설정.gif](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/27c8e1c0-e598-4c36-a793-5cd888424534/%E1%84%91%E1%85%B3%E1%84%85%E1%85%A9%E1%84%91%E1%85%B5%E1%86%AF_%E1%84%89%E1%85%A5%E1%86%AF%E1%84%8C%E1%85%A5%E1%86%BC.gif)

- Guest 로그인의 경우에는 접속이 끊기면 Guest 유저의 데이터가 DB에서 삭제되도록 구현했습니다.

> **Main**
> 
- 메인 화면에서는 프로필 상세 페이지, 상점, 랭킹, 라이브러리, 게임방법, 게임 화면으로 전환할 수 있습니다.

![메인 화면.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/36083386-fe58-46c8-861c-d4032aef9649/%E1%84%86%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AB_%E1%84%92%E1%85%AA%E1%84%86%E1%85%A7%E1%86%AB.png)

- 프로필 상세 화면에서는 로그인한 유저의 전적, 티어를 볼 수 있고, 프로필 변경을 할 수 있습니다.

![프로필 세부 정보.gif](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/11646aa3-e29d-4e84-a6ab-9725b1ae3101/%E1%84%91%E1%85%B3%E1%84%85%E1%85%A9%E1%84%91%E1%85%B5%E1%86%AF_%E1%84%89%E1%85%A6%E1%84%87%E1%85%AE_%E1%84%8C%E1%85%A5%E1%86%BC%E1%84%87%E1%85%A9.gif)

- 상점 화면에서는 게임 시작 전에 사용할 아이템들을 구입할 수 있습니다.

![상점.gif](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/400d2d2d-9450-48f3-8eef-e77a5f3dbcca/%E1%84%89%E1%85%A1%E1%86%BC%E1%84%8C%E1%85%A5%E1%86%B7.gif)

- 랭킹 화면에서는 DB에 저장되어 있는 유저들의 랭킹을 볼 수 있습니다.

![랭킹.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/051b13f7-6511-4c76-bdb0-35e717c29056/%E1%84%85%E1%85%A2%E1%86%BC%E1%84%8F%E1%85%B5%E1%86%BC.png)

- 라이브러리 화면에서는 게임에 나오는 카드들의 목록을 볼 수 있습니다. 카드를 터치하면 카드를 크게 볼 수 있습니다.

![라이브러리.gif](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/ba78497a-0c08-4475-b9fb-1bbb2fbd4466/%E1%84%85%E1%85%A1%E1%84%8B%E1%85%B5%E1%84%87%E1%85%B3%E1%84%85%E1%85%A5%E1%84%85%E1%85%B5.gif)

- 게임 방법 화면에서는 게임의 규칙을 볼 수 있습니다.
- 스타트 버튼을 눌러 방 목록 페이지로 넘어갑니다.

![info.gif](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/730000fa-26d4-43c9-8cf9-a6169853c934/info.gif)

> **방 목록**
> 
- 코드를 입력해 방을 만들고, 같은 코드를 입력하여 만들어진 방에 입장할 수 있습니다.
- 방이 생성, 삭제되거나 방의 정보가 변경되면 접속해있는 모든 유저들의 방 목록 화면이 갱신됩니다.

![방 목록.gif](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/67004719-48ad-4145-b242-e8435001afbb/%E1%84%87%E1%85%A1%E1%86%BC_%E1%84%86%E1%85%A9%E1%86%A8%E1%84%85%E1%85%A9%E1%86%A8.gif)

> **방 세부 정보**
> 
- 방 세부 정보 화면에는 현재 방에 접속해 있는 유저들의 정보가 표시됩니다.
- 유저들은 레디 상태를 토글 방식으로 변경할 수 있습니다.
- 방 안의 유저 두명이 모두 레디 상태라면 방장이 매치 버튼을 눌러서 게임을 시작할 수 있습니다.
- 게임 시작 전 상점에서 샀던 아이템을 사용할지에 대한 여부를 선택할 수 있습니다. (게임 적용은 미구현)

![방 세부 정보.gif](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/c452f56c-02f0-48aa-9832-69fc5f72e03c/%E1%84%87%E1%85%A1%E1%86%BC_%E1%84%89%E1%85%A6%E1%84%87%E1%85%AE_%E1%84%8C%E1%85%A5%E1%86%BC%E1%84%87%E1%85%A9.gif)

> **게임**
> 
- 게임 화면에서는 게임에 참여한 두 플레이어가 실시간으로 턴제 게임을 즐길 수 있습니다.
- 화면 하단 입력창에 베팅할 금액을 선택할 수 있고, 먼저 베팅을 하면 상대방에게 베팅을 했다는 UI가 제공됩니다.
- 두 플레이어가 모두 베팅을 완료하면, 라운드가 진행되고 베팅의 결과가 팝업창으로 나타납니다.

![게임.gif](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/d5a5b3e3-79a2-490b-96d8-3fb95aae64f9/%E1%84%80%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%B7.gif)

- 15라운드가 모두 지나면 게임 결과를 반영해 두 플레이어에게 게임 결과 화면이 표시됩니다.

![승리 화면.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/b7c28762-0cf8-4375-8d23-3e6ea8a984da/%E1%84%89%E1%85%B3%E1%86%BC%E1%84%85%E1%85%B5_%E1%84%92%E1%85%AA%E1%84%86%E1%85%A7%E1%86%AB.png)

![lose.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/dd31b21d-e89d-4e55-ab88-ef48b11e5c2f/lose.png)

<aside>
💡 **게임의 개요와 룰 정리**

**개요:**

- **플레이어 수**: 2인용
- **라운드 수**: 15라운드
- **시작 조건**: 플레이어 각각 10000골드 보유, 초기 국력 0
- **목표**: 더 많은 카드를 구매하여 국력을 높이고, 최종적으로 더 높은 국력을 가진 플레이어가 승리

**게임의 진행:**

1. **카드 제공**: 매 라운드마다 중앙에 카드가 제공됨 (카드마다 국력 값이 있음)
2. **배팅**: 두 플레이어는 각자 배팅할 골드를 입력
    - 두 플레이어가 모두 배팅할 골드를 입력하면 배팅 결과를 계산
3. **배팅 결과**:
    - 더 높은 골드를 배팅한 플레이어가 카드를 구매함
    - 두 플레이어 모두 배팅한 골드를 잃음
    - **동점인 경우**: 먼저 배팅한 플레이어가 해당 카드를 구매하고, 두 플레이어 모두 배팅한 골드를 잃음
4. **국력 증가**: 카드를 구매한 플레이어의 국력이 해당 카드의 국력만큼 증가
5. **라운드 종료**: 15라운드가 종료되면 게임이 끝남
6. **승리 조건**: 15라운드 종료 시 더 높은 국력을 가진 플레이어가 승리

**세부 규칙:**

- **최대 배팅 금액**: 플레이어는 현재 보유한 골드보다 더 많은 금액을 배팅할 수 없음
- **라운드 진행**: 각 라운드는 두 플레이어가 배팅을 완료하고 결과가 발표된 후 다음 라운드로 진행됨
- **게임 종료**: 15라운드가 종료되면 게임이 종료되고 승리자가 결정됨
</aside>

## APK

---

[AuctionKingdom.apk](https://drive.google.com/file/d/1VVsszM2t84sn7B0f8LFBty8Ui0JSRg0C/view?usp=sharing)

## Drawing

---

- Thumbnail
    - logo
        
        ![logo.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/ab63feea-d10a-4ecf-866d-0c5d7788441c/logo.png)
        
    - background
        
        ![backgroundimg.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/c361bbe8-e775-47bb-aa98-f23754716744/backgroundimg.png)
        
- Character
    
    ![profile_image_1.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/6d362d02-93bf-4c29-981d-fadee1eda24a/profile_image_1.png)
    
    ![profile_image_2.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/8310de68-e03e-47ec-ac53-1bae60de4e0d/profile_image_2.png)
    
    ![profile_image_3.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/d8c6dde6-6971-4def-afac-6ff92691d131/profile_image_3.png)
    
    ![profile_image_4.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/da7ad28b-0aa4-4072-8495-130b53795433/profile_image_4.png)
    
- Icon
    - icon_default
        
        ![shop_icon.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/a018485e-9b2b-4f3a-a51f-e0889a1abd77/shop_icon.png)
        
        ![trophy_icon.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/812e8f70-9413-4918-a16d-77ca18a310f1/trophy_icon.png)
        
        ![cards_icon.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/260d86fa-35f1-488c-be1b-cd2bb4b5be90/cards_icon.png)
        
        ![info_icon.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/5072386a-557e-4491-91a6-e928a9869bfb/info_icon.png)
        
    - icon_pop
        
        ![shop_pop_icon.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/e3e24e9d-e2bc-4c05-ac31-21f95da15387/shop_pop_icon.png)
        
        ![rank_pop_icon.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/af67df59-d19f-4e00-b9dd-1cb37832390d/rank_pop_icon.png)
        
        ![library_pop_icon.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/ad1e18fc-2b28-4e33-8463-fa83b2df4bc5/library_pop_icon.png)
        
        ![info_pop_icon.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/febbe236-db49-48dc-a790-ea23ac5951bd/info_pop_icon.png)
        
    - icon_shop
        
        ![item_icon_1.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/27974a96-3ec0-4d73-9f2e-15a8e01dcab4/item_icon_1.png)
        
        ![item_icon_2.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/8811e70a-b77f-48eb-bbcf-862fe0793edb/item_icon_2.png)
        
        ![item_icon_3.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/73092be4-294c-4d2c-b7c2-c4f7e2d62ab0/item_icon_3.png)
        
- Card
    
    ![archer.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/0c46f694-7ed7-40f9-8189-00efaa33d887/archer.png)
    
    ![castle.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/8957973e-5d78-4586-b35d-15d376252d2e/castle.png)
    
    ![cavalry.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/bf04da65-4138-4407-b205-56a6f75c2862/cavalry.png)
    
    ![craft.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/17648bc7-ac2f-40a4-8b12-f10e7fedd1f1/craft.png)
    
    ![farmer.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/6155047b-b7c6-4717-bb7e-5f7a92c55127/farmer.png)
    
    ![merchant.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/3aafdaf8-f78f-42b9-8017-374bab2931e8/merchant.png)
    
    ![scholar.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/094e6485-688f-4a75-acaf-f02fc06f93d9/scholar.png)
    
    ![soldier.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/5a3e9e1b-4c09-478e-93b4-440f67344ca8/soldier.png)
    
    ![spear.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/9f44a96b-c5c9-4d1d-b3c4-3f004b7a6f5c/spear.png)
    
    ![wall.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/993f1d12-52bc-429b-89dc-cf4df11251e5/wall.png)
    
- Results
    - win
        
        ![win_background1.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/5cced2b9-80be-41ca-840c-337b587eee94/win_background1.png)
        
        ![win_background2.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/59efeda7-7790-4f45-ab3a-09ab89833a76/win_background2.png)
        
        ![win_background3.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/1a8ce2f1-b6b5-4d1a-b2dd-855abd2535f8/win_background3.png)
        
        ![win_background4.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/83d76eb3-4f1d-4264-9b97-f6f5c94ab776/win_background4.png)
        
    - lose
        
        ![lose_background1.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/612ca43a-b681-42dd-bfda-023e17f58bef/lose_background1.png)
        
        ![lose_background2.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/6c5d2d4e-1069-438e-83be-07162c22a624/lose_background2.png)
        
        ![lose_background3.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/413487d8-2fc0-429d-8761-1c596ac2e17d/lose_background3.png)
        
        ![lose_background4.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/f0bd86e7-3ff4-4278-b990-d7fc0dccf3e0/lose_background4.png)
        
- etc.
    - stamp
        
        ![stamp.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/66ff56cd-1ca9-4ed7-b245-5c617e9d420f/stamp.png)
        
    - button
        
        ![cancel_btn.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/c1bc7ceb-1628-493c-8cfa-34d03d116001/cancel_btn.png)
        
        ![create_button.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/eba4ebfa-7ab8-4699-ad21-179f1de858a6/create_button.png)
        
        ![exit_button.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/e7e2feeb-679b-4701-a3c5-4baecf687877/exit_button.png)
        
        ![match_button.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/e44d83d5-26d0-4e23-9397-89186003467a/match_button.png)
        
        ![ok_button.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/9e268d9d-12b3-44cf-af39-14938cdb0de9/ok_button.png)
        
        ![join_button.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/49a239ef-041d-4d82-98ce-b393698d23d5/join_button.png)
        
        ![edit_btn.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/79690a2b-b280-403c-962a-2f5dd9314d34/edit_btn.png)
        
        ![ready_button.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/7a513ab8-6c40-4762-8a18-bce25a6e5ca2/ready_button.png)
        
        ![start_button.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/42590e0b-7c11-4d8c-b0a6-bd31e27f07ca/start_button.png)
        
        ![buy_button.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/3d049eb4-3e97-4869-824f-94576e7e7bdb/buy_button.png)
        
    - title
        
        ![profile_title.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/bc622d64-2bb8-4618-a1cf-1478d9bd4045/profile_title.png)
        
        ![library_title.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/66b15242-2106-46a8-a795-58e8a46f7410/library_title.png)
        
        ![rank_title.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/948a20f6-d0a7-4e1a-8d62-55e89de68666/rank_title.png)
        
        ![shop_title.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/315a75b6-3b65-44c5-b28c-0d8e6e46e131/shop_title.png)
        
        ![info_title.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/c46bf935-5962-412f-9b3b-2bb2a8748d4d/info_title.png)
        
        ![edit_title.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/796392a5-9eea-4990-89d2-5b15dfd2c9c4/edit_title.png)
