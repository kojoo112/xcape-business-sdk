const SUCCESS = 'SUCCESS';
const GENERAL = 'general';
const OPEN_ROOM = 'openRoom';
const SLIDER = 'SLIDER';
const BLOCK = 'BLOCK';
const CREATE = 'CREATE';
const UPDATE = 'UPDATE';
const SAVE_SUCCESS = '성공적으로 저장했습니다. 👏';
const SAVE_FAIL = '저장 중 에러가 발생했습니다. 😭';

const JSON_FILE_NAME = 'release.json';
const JSON_FILE_TYPE = {
    MERCHANT: 'merchant',
    THEME: 'theme',
    BANNER: 'banner',
    HINT: 'hint',
    TAG: 'tag',
    VIEW : 'view',
    ABILITY: 'ability'
}
const FAKE_RESERVATION = (reservationId) => {
    return {
        id: reservationId,
        reservedBy: 'XCAPE',
        phoneNumber: '01000000000',
        participantCount: 2,
    }
}

const ANSWER = 'ANSWER';
const KEYPAD_LOCK = 'KEYPAD_LOCK';
const ALPHABET_SCROLL_LOCK = 'ALPHABET_SCROLL_LOCK';
const NUMBER_SCROLL_LOCK = 'NUMBER_SCROLL_LOCK';
const BUTTON_PADLOCK = 'BUTTON_PADLOCK';
const DIRECTION_PADLOCK = 'DIRECTION_PADLOCK';
