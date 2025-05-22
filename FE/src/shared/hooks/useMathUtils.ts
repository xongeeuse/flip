/**
 * 도(degree)를 라디안(radian)으로 변환하는 함수
 * @param degree 변환할 각도 (도)
 * @returns 라디안 값
 */
export const degreeToRadian = (degree: number): number => {
  return degree * (Math.PI / 180);
};

/**
 * 라디안(radian)을 도(degree)로 변환하는 함수
 * @param radian 변환할 각도 (라디안)
 * @returns 도(degree) 값
 */
export const radianToDegree = (radian: number): number => {
  return radian * (180 / Math.PI);
};

/**
 * 3D 수학 관련 유틸리티 훅
 */
export const useMathUtils = () => {
  return {
    degreeToRadian,
    radianToDegree,
  };
};
