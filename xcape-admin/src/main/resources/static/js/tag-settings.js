let storageList = null;
let tagList = null;
let viewList = null;
let hintList = null;

const getStorageList = () => {
    axios.get('/storage').then(({data}) => {
        if (data.resultCode === SUCCESS) {
            storageList = data.result.filter((storage) => storage.isUsed);
        }
    });
}

const getTagList = () => {
    axios.get('/tags').then(({data}) => {
        if (data.resultCode === SUCCESS) {
            tagList = data.result;
        }
    });
}

const getHintList = () => {
    axios.get('/hints').then(({data}) => {
        if (data.resultCode === SUCCESS) {
            hintList = data.result;
        }
    });
}

const getViewList = () => {
    axios.get('/views').then(({data}) => {
        if (data.resultCode === SUCCESS) {
            viewList = data.result;
        }
    });
}
/**
 *  초기 데이터 불러오기
 */
getStorageList();
getHintList();
getTagList();
getViewList();

const clearTagList = () => {
    document.querySelector('#tagListContainer').innerHTML = document.querySelector('#tagGuide').innerHTML
}

const checkTagListDom = () => {
    const tagListDom = document.querySelector('#tagList');
    return !!tagListDom;
}

const hasCamera = () => {
    return [...document.querySelectorAll('#tagList > .card')].some((view) => view.dataset.type === 'CAMERA');
}

const hasAnswer = () => {
    return [...document.querySelectorAll('#tagList > .card')].some((view) => view.dataset.type === 'ANSWER' || view.dataset.type === 'KEYPAD_LOCK' || view.dataset.type === 'ALPHABET_SCROLL_LOCK' || view.dataset.type === 'NUMBER_SCROLL_LOCK' || view.dataset.type === 'BUTTON_PADLOCK' || view.dataset.type === 'DIRECTION_PADLOCK');
}

// 업로드
const uploadJsonFile = async (data, type) => {
    let form = new FormData();
    form.append('file', new File([JSON.stringify(data)], JSON_FILE_NAME));
    form.append('type', type);

    return await axios.put('/json', form);
};

const getTagListByThemeId = (themeId) => {
    return tagList.filter((tag) => tag.themeId === parseInt(themeId))
        .sort((a, b) => a.name.localeCompare(b.name));
}

// 발행 버튼 클릭시 이벤트
document.querySelector('#jsonPublishButton').addEventListener('click', async () => {
    try {
        const responses = await axios.all([
            axios.get('/tags'),
            axios.get('/views')
        ]);

        const [tagResponse, viewResponse] = responses;
        await handleDataResponse(tagResponse, viewResponse);

    } catch (error) {
        alert('발행에 실패했습니다.');
    }
});

const handleDataResponse = async (tagResponse, viewResponse) => {
    const {resultCode: tagResultCode, result: tagResult} = tagResponse.data;
    const {resultCode: viewResultCode, result: viewResult} = viewResponse.data;

    if (tagResultCode !== SUCCESS || viewResultCode !== SUCCESS) {
        throw new Error('조회 실패');
    }

    const tagUploadResponse = await uploadJsonFile(tagResult, JSON_FILE_TYPE.TAG);
    const viewUploadResponse = await uploadJsonFile(viewResult, JSON_FILE_TYPE.VIEW);

    const tagPath = tagUploadResponse.data;
    const viewPath = viewUploadResponse.data;

    if (!tagPath || !viewPath) {
        throw new Error('파일 업로드 실패');
    }
    alert(`태그 : ${tagPath} 및 뷰 : ${viewPath}\n 발행이 완료되었습니다.`);
};

const isPadlock = (type) => {
    if (type === ANSWER
        || type === KEYPAD_LOCK
        || type === ALPHABET_SCROLL_LOCK
        || type === NUMBER_SCROLL_LOCK
        || type === BUTTON_PADLOCK
        || type === DIRECTION_PADLOCK) {
        return true;
    } else {
        return false;
    }
}

const convertPadlockTypeToKorean = (type) => {
    if (type === ANSWER) {
        return '입력 형식';
    } else if (type === KEYPAD_LOCK) {
        return '키패드 형식';
    } else if (type === ALPHABET_SCROLL_LOCK) {
        return '알파벳 스크롤 형식';
    } else if (type === NUMBER_SCROLL_LOCK) {
        return '숫자 스크롤 형식';
    } else if (type === BUTTON_PADLOCK) {
        return '버튼식 자물쇠 형식';
    } else if (type === DIRECTION_PADLOCK) {
        return '방향 자물쇠 형식';
    }
}

const getStorageItemList = () => {
    const selectedViewType = document.querySelector('input[name="viewType"]:checked').value;
    const activeTheme = document.querySelector('button.list-group-item.active')

    if (!activeTheme) {
        return;
    }

    const {themeId} = activeTheme.dataset;

    let previewDom;
    let previewListDom = '';

    if (selectedViewType !== 'HINT') {
        const storageItemListByThemeIdAndViewType = storageList.filter((storage) => storage.themeId === parseInt(themeId) && storage.fileType === selectedViewType);

        if (selectedViewType === 'IMAGE') {
            previewDom = document.querySelector('#imageCard').innerHTML;
        } else if (selectedViewType === 'AUDIO') {
            previewDom = document.querySelector('#audioCard').innerHTML;
        } else if (selectedViewType === 'VIDEO') {
            previewDom = document.querySelector('#videoCard').innerHTML;
        }

        if (storageItemListByThemeIdAndViewType.length > 0) {
            storageItemListByThemeIdAndViewType.forEach(({url, filename, height, answer, targetTagId}) => {
                previewListDom += interpolate(previewDom, {url, filename, height, answer, targetTagId});
            });
        } else {
            previewListDom = document.querySelector('#emptyTemplate').innerHTML;
        }

    } else {
        previewDom = document.querySelector('#hintCard').innerHTML;

        const hintListByThemeId = hintList.filter((hint) => hint.themeId === parseInt(themeId) && hint.isUsed);

        if (hintListByThemeId.length > 0) {
            hintListByThemeId.forEach(({message1, message2}) => {
                previewListDom += interpolate(previewDom, {message1, message2});
            });
        } else {
            previewListDom = document.querySelector('#emptyTemplate').innerHTML;
        }
    }

    document.querySelector('#storageItemList').innerHTML = previewListDom;
}

const storageItemListDom = document.querySelector('#storageItemList');
Sortable.create(storageItemListDom, {
    group: {
        name: 'shared', pull: 'clone', put: false
    }, animation: 10, sort: false
});

const initTagListDom = () => {
    const containerDom = document.querySelector('#tagListContainer');
    containerDom.innerHTML = document.querySelector('#tagListTemplate').innerHTML;

    const tagListDom = document.querySelector('#tagList');
    Sortable.create(tagListDom, {
        group: 'shared', swap: true, swapClass: 'bg-secondary', animation: 150, removeOnSpill: true
    });
}

const getViewListByTagId = (tagId) => {
    return viewList.filter((view) => view.tagId === parseInt(tagId))
        .sort((a, b) => a.orders - b.orders);
}

document.querySelector('#merchantSelect').addEventListener('change', (e) => {
    const merchantId = e.target.value;

    if (merchantId) {
        axios.get(`/merchants/${merchantId}/themes`).then((res) => {
            const {result, resultCode} = res.data;
            if (resultCode === SUCCESS) {
                const themeOptionTemplate = document.querySelector('#themeOptionTemplate').innerHTML;

                let themeOptionHtml = document.querySelector('#defaultSelect').innerHTML;
                result.forEach(theme => {
                    themeOptionHtml += interpolate(themeOptionTemplate, {id: theme.id, name: theme.nameKo});
                });

                document.querySelector('#themeSelect').innerHTML = themeOptionHtml;
            }
        });
    }
});

document.querySelector('#themeSelect').addEventListener('change', (e) => {
    const themeId = e.target.value;

    if (themeId) {
        getTagListByThemeId(themeId);
    }
});

document.querySelectorAll('button.list-group-item').forEach(theme => {
    theme.addEventListener('click', (e) => {
        const {classList, dataset} = e.currentTarget;

        document.querySelectorAll('button.list-group-item').forEach(merchant => merchant.classList.remove('active'));
        classList.add('active');

        const {themeId} = dataset;
        const tagListByThemeId = getTagListByThemeId(themeId);

        const tagOptionTemplate = document.querySelector('#tagOptionsTemplate').innerHTML;

        let tagOptionListHtml = document.querySelector('#defaultSelect').innerHTML
        tagListByThemeId.forEach((tag) => {
            tagOptionListHtml += interpolate(tagOptionTemplate, {id: tag.id, name: tag.name});
        });

        document.querySelector('#tagSelect').innerHTML = tagOptionListHtml;
        document.querySelector('#targetTagId').innerHTML = tagOptionListHtml;

        getStorageItemList();
        clearTagList();
    });
});

document.querySelectorAll('input[name="viewType"]').forEach((button) => {
    button.addEventListener('click', () => {
        getStorageItemList();
    });
});

document.querySelector('#tagSelect').addEventListener('change', (e) => {
    const tagId = e.target.value;
    if (tagId === '') {
        clearTagList();
    } else {
        initTagListDom();
        const viewListByTagId = getViewListByTagId(tagId);

        let viewListDom = '';
        viewListByTagId.forEach((view) => {
            if (isPadlock(view.type)) {
                const padlockCard = document.querySelector('#padlockCard').innerHTML;
                const targetTag = tagList.find((tag) => tag.id === view.targetTagId);

                viewListDom += interpolate(padlockCard, {
                    type: view.type,
                    answer: view.answer,
                    typeKo: convertPadlockTypeToKorean(view.type),
                    targetTagId: view.targetTagId,
                    targetTagName: targetTag.name
                });
            } else {
                let previewDom;

                if (view.type === 'IMAGE') {
                    previewDom = document.querySelector('#imageCard').innerHTML;
                } else if (view.type === 'AUDIO') {
                    previewDom = document.querySelector('#audioCard').innerHTML;
                } else if (view.type === 'VIDEO') {
                    previewDom = document.querySelector('#videoCard').innerHTML;
                } else if (view.type === 'CAMERA') {
                    previewDom = document.querySelector('#cameraCard').innerHTML;
                } else if (view.type === 'HINT') {
                    previewDom = document.querySelector('#hintCard').innerHTML;
                }

                viewListDom += interpolate(previewDom, {src: view.url, ...{...view}});
            }

            document.querySelector('#tagList').innerHTML = viewListDom;
        });
    }
});

document.querySelector('#cameraAddButton').addEventListener('click', () => {
    if (checkTagListDom()) {
        if (hasCamera()) {
            alert('카메라가 이미 존재합니다.');
        } else {
            const height = document.querySelector('#cameraHeight').value;
            if (height) {
                const cameraCard = document.querySelector('#cameraCard').innerHTML;
                const cameraCardDom = interpolate(cameraCard, {height});
                document.querySelector('#tagList').insertAdjacentHTML('beforeend', cameraCardDom);
            }
        }
    } else {
        alert('태그를 선택해주세요.');
    }
});

document.querySelector('#padlockCategory').addEventListener('change', (e) => {
    document.querySelectorAll('.answerInput').forEach((e) => {
        e.classList.add('d-none')
    });

    const {value} = e.target;

    if (value === 'ANSWER') {
        document.querySelector('#answerArea').classList.remove('d-none');
    } else if (value === 'KEYPAD_LOCK') {
        document.querySelector('#keypadArea').classList.remove('d-none');
    } else if (value === 'ALPHABET_SCROLL_LOCK') {
        document.querySelector('#alphabetScrollLockArea').classList.remove('d-none');
    } else if (value === 'NUMBER_SCROLL_LOCK') {
        document.querySelector('#numberScrollLockArea').classList.remove('d-none');
    } else if (value === 'BUTTON_PADLOCK') {
        document.querySelector('#buttonPadlockArea').classList.remove('d-none');
    } else if (value === 'DIRECTION_PADLOCK') {
        document.querySelector('#directionPadlockArea').classList.remove('d-none');
    }
});

const buttonPadlockChangeEvent = () => {
    document.querySelectorAll('#buttonPadlockArea .btn-group button').forEach((e) => {
        const {number} = e.dataset;
        const numberBadge = document.querySelector(`.number[data-number="${number}"]`);
        if (numberBadge) {
            e.classList.add('disabled');
        } else {
            e.classList.remove('disabled');
        }

    });
}

const sortPadlockNumbers = () => {
    const buttonPadlock = document.querySelector('#buttonPadlock');
    const sortedNumberList = [...document.querySelectorAll('.number')].sort((a, b) => a.innerText - b.innerText);

    buttonPadlock.innerHTML = '';
    sortedNumberList.forEach((numberBadge) => {
        buttonPadlock.appendChild(numberBadge);
    });
}

document.querySelectorAll('#buttonPadlockArea .btn-group button').forEach((button) => {
    button.addEventListener('click', (e) => {

        const buttonPadlockDom = document.querySelector('#buttonPadlock');
        const buttonPadlockBadgeDom = document.querySelector('#numberBadge').innerHTML

        const {number} = e.target.dataset;
        buttonPadlockDom.innerHTML += interpolate(buttonPadlockBadgeDom, {number});

        buttonPadlockChangeEvent();
        sortPadlockNumbers();
    });
});

document.querySelector('#removeButtonPadlockNumbers').addEventListener('click', () => {
    document.querySelector('#buttonPadlock').innerHTML = '';
    buttonPadlockChangeEvent();
});

document.querySelectorAll('#directionPadlockArea .btn-group button').forEach((button) => {
    button.addEventListener('click', (e) => {
        const directionPadlockDom = document.querySelector('#directionPadlock');
        const directionBadgeDom = document.querySelector('#directionBadge').innerHTML

        const {direction} = e.target.dataset;
        directionPadlockDom.innerHTML += interpolate(directionBadgeDom, {direction})
    });
});

const addPadlock = (type, answer) => {
    if (hasAnswer()) {
        alert('문제가 이미 존재합니다.');
    } else {
        const targetTagId = document.querySelector('#targetTagId').value;
        const targetTagName = document.querySelector('#targetTagId option:checked').innerText;
        if (targetTagId === '') {
            alert('이동 할 태그를 선택해주세요.');
        } else {
            const padlockCard = document.querySelector('#padlockCard').innerHTML;

            const answerCardDom = interpolate(padlockCard, {
                type,
                answer,
                typeKo: convertPadlockTypeToKorean(type),
                targetTagId,
                targetTagName
            });

            document.querySelector('#tagList').insertAdjacentHTML('beforeend', answerCardDom);
        }
    }
}

document.querySelector('#answerAddButton').addEventListener('click', () => {
    if (checkTagListDom()) {
        const answer = document.querySelector('#answer').value;
        addPadlock(ANSWER, answer);
    } else {
        alert('태그를 선택해주세요.');
    }
});


document.querySelector('#keypadAddButton').addEventListener('click', () => {
    const keypadLockValue = document.querySelector('#keypadLock').value;
    const numberRegexp = /^[0-9]+$/g;

    if (numberRegexp.test(keypadLockValue)) {
        addPadlock(KEYPAD_LOCK, keypadLockValue);
    } else {
        alert('숫자만 입력 가능합니다.');
    }
});

document.querySelector('#alphabetAddButton').addEventListener('click', () => {
    const alphabetValue = document.querySelector('#alphabet').value;
    const alphabetRegexp = /^[a-zA-Z]+$/g;

    if (alphabetRegexp.test(alphabetValue) && alphabetValue.length === 4) {
        addPadlock(ALPHABET_SCROLL_LOCK, alphabetValue.toUpperCase());
    } else {
        alert('영어만 입력 가능합니다.');
    }
});

document.querySelector('#buttonPadlockAddButton').addEventListener('click', () => {
    let numberStr = '';
    document.querySelectorAll('#buttonPadlock .number').forEach((number) => {
        numberStr += number.dataset.number;
    })
    addPadlock(BUTTON_PADLOCK, numberStr);
});

document.querySelector('#numberPadlockAddButton').addEventListener('click', () => {
    const numberPadlockValue = document.querySelector('#numberPadlock').value;
    const numberRegexp = /^[0-9]+$/g;

    if (numberRegexp.test(numberPadlockValue) && numberPadlockValue.length === 3) {
        addPadlock(NUMBER_SCROLL_LOCK, numberPadlockValue)
    } else {
        alert('3개의 숫자만 가능합니다.');
    }
});

document.querySelector('#removeAllDirection').addEventListener('click', () => {
    document.querySelector('#directionPadlock').innerHTML = '';
});

document.querySelector('#directionPadlockAddButton').addEventListener('click', () => {
    const directionArr = [];

    document.querySelectorAll('#directionPadlock .badge').forEach((badge) => {
        directionArr.push(badge.dataset.direction);
    });

    addPadlock(DIRECTION_PADLOCK, directionArr.toString());
});

document.querySelector('#saveTagButton').addEventListener('click', () => {
    const tagId = document.querySelector('#tagSelect').value;

    if (tagId) {
        const viewListDom = document.querySelectorAll('#tagList .card');

        const viewList = [...viewListDom].map((view, orders) => {
            return {...view.dataset, tagId, orders}
        });

        axios.put('/views', viewList).then(({data}) => {
            if (data.resultCode === SUCCESS) {
                getViewList();
                popAlert('success', '성공', '태그를 저장 했습니다.', 1000);
            } else {
                popAlert('error', '실패', data.resultMessage, 1000);
            }
        });
    } else {
        alert('태그를 선택해주세요.');
    }
});

document.querySelector('#tagCreateButton').addEventListener('click', () => {
    const createTagForm = document.querySelector('form[name="createTagForm"]');
    const merchantId = createTagForm.merchantId.value;
    const themeId = createTagForm.themeId.value;
    const name = createTagForm.tagName.value;

    if (merchantId !== '' && themeId !== '' && name !== '') {
        const param = {
            merchantId,
            themeId,
            name
        }
        axios.post('/tags', param).then((res) => {
            const {resultCode, resultMessage} = res.data;
            if (resultCode === SUCCESS) {
                alert('태그가 생성되었습니다.');
                getTagList();
            } else {
                alert(`실패 : ${resultMessage}`);
            }
        });
    } else {
        alert('가맹점, 테마, 태그이름을 확인해주세요.');
    }
});
