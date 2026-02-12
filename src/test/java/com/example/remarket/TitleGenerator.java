package com.example.remarket;

import com.example.remarket.domain.item.entity.enumerate.Category;

import java.util.Random;

// 별도의 TitleGenerator 클래스
class TitleGenerator {
    private final Random random = new Random();

    public String generateTitle(Category category, int index) {
        StringBuilder title = new StringBuilder();

        switch (category) {
            case DIGITAL_DEVICE:
                title.append(generateDigitalDeviceTitle());
                break;
            case MEN_FASHION_ACCESSORIES:
                title.append(generateMenFashionTitle());
                break;
            case WOMEN_CLOTHING:
                title.append(generateWomenClothingTitle());
                break;
            case HOME_APPLIANCE:
                title.append(generateHomeApplianceTitle());
                break;
            case SPORTS_LEISURE:
                title.append(generateSportsLeisureTitle());
                break;
            case BOOK:
                title.append(generateBookTitle());
                break;
            case FURNITURE_INTERIOR:
                title.append(generateFurnitureTitle());
                break;
            case BABY_CHILD:
                title.append(generateBabyChildTitle());
                break;
            default:
                title.append("중고 ").append(category.getDescription());
        }

        // 상태 표현 추가 (30% 확률로 추가)
        if (random.nextInt(100) < 30) {
            String[] conditions = {"새상품", "거의새것", "사용감적음", "상태양호", "사용감있음"};
            title.append(" [").append(conditions[random.nextInt(conditions.length)]).append("]");
        }

        // 색상 추가 (40% 확률로 추가)
        if (random.nextInt(100) < 40) {
            title.append(" ").append(generateColor());
        }

        title.append(" - ").append(index);
        return title.toString();
    }

    private String generateDigitalDeviceTitle() {
        // 브랜드
        String[] brands = {"아이폰", "갤럭시", "맥북", "아이패드", "에어팟", "갤럭시북", "LG그램", "한성컴퓨터"};
        String brand = brands[random.nextInt(brands.length)];

        // 모델 (브랜드별로 다른 모델)
        String model;
        if (brand.contains("아이폰")) {
            String[] iphoneModels = {"6", "6s", "7", "8", "X", "11", "12", "13", "14", "15", "SE"};
            model = iphoneModels[random.nextInt(iphoneModels.length)];
        } else if (brand.contains("갤럭시")) {
            String[] galaxyModels = {"S20", "S21", "S22", "S23", "S24", "A51", "A71", "A54", "Z플립", "Z폴드"};
            model = galaxyModels[random.nextInt(galaxyModels.length)];
        } else if (brand.contains("맥북")) {
            String[] macbookModels = {"에어 M1", "에어 M2", "에어 M3", "프로 13", "프로 14", "프로 16"};
            model = macbookModels[random.nextInt(macbookModels.length)];
        } else {
            String[] otherModels = {"프로", "에어", "플러스", "미니", "울트라", "라이트"};
            model = otherModels[random.nextInt(otherModels.length)];
        }

        // 저장 용량 (30% 확률로 추가)
        StringBuilder storage = new StringBuilder();
        if (random.nextInt(100) < 30) {
            String[] storages = {"64GB", "128GB", "256GB", "512GB", "1TB"};
            storage.append(" ").append(storages[random.nextInt(storages.length)]);
        }

        // 색상 (20% 확률로 추가)
        String color = "";
        if (random.nextInt(100) < 20) {
            String[] colors = {"블랙", "화이트", "실버", "골드", "그래파이트", "블루", "퍼플"};
            color = " " + colors[random.nextInt(colors.length)];
        }

        return brand + " " + model + storage.toString() + color + " 판매";
    }

    private String generateMenFashionTitle() {
        // 의류 유형
        String[] types = {"반팔티셔츠", "긴팔티셔츠", "후드티", "맨투맨", "셔츠", "정장", "청바지", "트레이닝복", "래쉬가드", "자켓", "코트"};
        String type = types[random.nextInt(types.length)];

        // 브랜드
        String[] brands = {"나이키", "아디다스", "퓨마", "뉴발란스", "언더아머", "챔피온", "게스", "디젤", "리바이스", "젝시믹스"};
        String brand = brands[random.nextInt(brands.length)];

        // 스타일 (30% 확률로 추가)
        String style = "";
        if (random.nextInt(100) < 30) {
            String[] styles = {"데일리", "캐주얼", "오피스", "스트릿", "빈티지", "클래식", "슬림핏", "루즈핏"};
            style = " " + styles[random.nextInt(styles.length)] + " 스타일";
        }

        // 사이즈 (50% 확률로 추가)
        String size = "";
        if (random.nextInt(100) < 50) {
            String[] sizes = {"S", "M", "L", "XL", "XXL", "FREE"};
            size = " [" + sizes[random.nextInt(sizes.length)] + "]";
        }

        return brand + " " + type + style + size + " 판매";
    }

    private String generateWomenClothingTitle() {
        // 의류 유형
        String[] types = {"원피스", "블라우스", "가디건", "점퍼", "코트", "청바지", "스커트", "티셔츠", "정장"};
        String type = types[random.nextInt(types.length)];

        // 브랜드
        String[] brands = {"자라", "H&M", "유니클로", "무신사", "쎄시봉", "스파오", "탑텐", "에잇세컨즈", "에이블리"};
        String brand = brands[random.nextInt(brands.length)];

        // 패턴 (20% 확률로 추가)
        String pattern = "";
        if (random.nextInt(100) < 20) {
            String[] patterns = {"스트라이프", "체크", "도트", "플라워", "프린트", "무지"};
            pattern = " " + patterns[random.nextInt(patterns.length)] + " 패턴";
        }

        // 소재 (15% 확률로 추가)
        String material = "";
        if (random.nextInt(100) < 15) {
            String[] materials = {"면", "린넨", "니트", "데님", "시폰", "실크", "가죽"};
            material = " " + materials[random.nextInt(materials.length)] + " 소재";
        }

        return brand + " " + type + pattern + material + " 판매";
    }

    private String generateHomeApplianceTitle() {
        // 가전 종류
        String[] appliances = {"냉장고", "세탁기", "TV", "에어컨", "청소기", "공기청정기", "전자레인지", "밥솥", "전기히터", "선풍기"};
        String appliance = appliances[random.nextInt(appliances.length)];

        // 브랜드
        String[] brands = {"삼성", "LG", "다이슨", "쿠쿠", "캐리어", "위니아", "한일", "필립스"};
        String brand = brands[random.nextInt(brands.length)];

        // 모델 정보 (40% 확률로 추가)
        String modelInfo = "";
        if (random.nextInt(100) < 40) {
            String[] models = {"2023년형", "2022년형", "신형", "구형", "풀옵션", "디럭스"};
            modelInfo = " " + models[random.nextInt(models.length)];
        }

        // 용량/사이즈 (가전 종류에 따라 다름)
        String capacity = "";
        if (appliance.contains("냉장고")) {
            String[] capacities = {"400L", "500L", "600L", "양문형", "4도어"};
            capacity = " " + capacities[random.nextInt(capacities.length)];
        } else if (appliance.contains("세탁기")) {
            String[] capacities = {"10kg", "12kg", "15kg", "트롬", "드럼"};
            capacity = " " + capacities[random.nextInt(capacities.length)];
        } else if (appliance.contains("TV")) {
            String[] sizes = {"55인치", "65인치", "75인치", "OLED", "QLED"};
            capacity = " " + sizes[random.nextInt(sizes.length)];
        }

        return brand + " " + appliance + capacity + modelInfo + " 판매";
    }

    private String generateSportsLeisureTitle() {
        // 종류
        String[] items = {"등산화", "자전거", "캠핑용품", "요가매트", "헬스기구", "수영복", "테니스라켓", "골프채", "축구화"};
        String item = items[random.nextInt(items.length)];

        // 브랜드
        String[] brands = {"노스페이스", "콜마운틴", "블랙야크", "이월드", "지프", "미즈노", "나이키", "아디다스", "요넥스"};
        String brand = brands[random.nextInt(brands.length)];

        // 특성 (30% 확률로 추가)
        String feature = "";
        if (random.nextInt(100) < 30) {
            if (item.contains("등산화")) {
                String[] features = {"방수", "중등산용", "하이탑", "가벼운"};
                feature = " " + features[random.nextInt(features.length)];
            } else if (item.contains("자전거")) {
                String[] features = {"MTB", "로드", "하이브리드", "전기"};
                feature = " " + features[random.nextInt(features.length)];
            }
        }

        return brand + feature + " " + item + " 판매";
    }

    private String generateBookTitle() {
        // 장르
        String[] genres = {"소설", "시집", "자기계발서", "경영", "IT", "요리", "여행", "역사", "과학"};
        String genre = genres[random.nextInt(genres.length)];

        // 제목 접두사
        String[] prefixes = {"이야기", "이야기의", "완전정복", "가이드", "입문", "마스터", "비밀", "기본", "실전"};
        String prefix = prefixes[random.nextInt(prefixes.length)];

        // 작가/출판사 (50% 확률로 추가)
        String author = "";
        if (random.nextInt(100) < 50) {
            String[] authors = {"김작가", "이지은", "박지성", "한빛미디어", "길벗", "위키북스"};
            author = " - " + authors[random.nextInt(authors.length)];
        }

        // 상태 (70% 확률로 추가 - 책은 상태가 중요)
        String condition = "";
        if (random.nextInt(100) < 70) {
            String[] conditions = {"깨끗함", "필기없음", "연필필기약간", "책갈피있음", "겉표지약간손상"};
            condition = " [" + conditions[random.nextInt(conditions.length)] + "]";
        }

        return genre + " " + prefix + condition + author + " 판매";
    }

    private String generateFurnitureTitle() {
        String[] furniture = {"책상", "의자", "소파", "책장", "침대", "수납장", "식탁", "화장대"};
        String item = furniture[random.nextInt(furniture.length)];

        String[] materials = {"원목", "합판", "철제", "유리", "가죽", "패브릭"};
        String material = materials[random.nextInt(materials.length)];

        // 색상 (60% 확률로 추가)
        String color = "";
        if (random.nextInt(100) < 60) {
            String[] colors = {"화이트", "블랙", "월넛", "네추럴", "그레이", "브라운"};
            color = " " + colors[random.nextInt(colors.length)];
        }

        // 사이즈 (가구 종류에 따라)
        String size = "";
        if (item.contains("책상") || item.contains("식탁")) {
            String[] sizes = {"1200", "1500", "1800", "L자형", "코너형"};
            size = " " + sizes[random.nextInt(sizes.length)] + "사이즈";
        } else if (item.contains("소파")) {
            String[] sizes = {"2인용", "3인용", "4인용", "카우치형"};
            size = " " + sizes[random.nextInt(sizes.length)];
        }

        return material + color + " " + item + size + " 판매";
    }

    private String generateBabyChildTitle() {
        String[] items = {"유모차", "아기띠", "장난감", "옷", "책", "식탁의자", "침대", "목욕용품"};
        String item = items[random.nextInt(items.length)];

        String[] brands = {"피셔프라이스", "브이텍", "듀퐁", "코코몽", "뽀로로", "아가방", "페도라"};
        String brand = brands[random.nextInt(brands.length)];

        // 연령대 (40% 확률로 추가)
        String age = "";
        if (random.nextInt(100) < 40) {
            String[] ages = {"신생아", "6개월", "12개월", "24개월", "36개월", "유아용", "영아용"};
            age = " [" + ages[random.nextInt(ages.length)] + "]";
        }

        return brand + " " + item + age + " 판매";
    }

    private String generateColor() {
        String[] colors = {"블랙", "화이트", "그레이", "네이비", "베이지", "브라운", "레드", "블루", "그린", "핑크", "퍼플", "옐로우", "오렌지"};
        return colors[random.nextInt(colors.length)];
    }

    public String generateContent(Category category, String title) {
        String[] templates = {
                "%s 상태 좋습니다. 직거래 선호합니다.",
                "%s 깨끗하게 관리했어요. 가격 협의 가능합니다.",
                "%s 중고로 판매합니다. 사진 참고해주세요.",
                "%s 실사용한 제품이에요. 문의 환영합니다.",
                "%s 최근에 구매했는데 필요없어서 판매해요.",
                "%s 박스포장까지 잘 보관하고 있습니다.",
                "%s A/S 보증기간 남아있습니다.",
                "%s 사용감 거의 없어요. 새것과 다름없습니다."
        };

        return String.format(templates[random.nextInt(templates.length)], title);
    }
}