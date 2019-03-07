// @flow strict

export class CustomTimelineElement {
  elementIndex: number;
  elementName: string;

  constructor(elementName: string) {
    if (elementName === undefined) {
      throw Error('Invalid Element Name: Classes extending CustomTimelineElement must set their element name via a super(elementName) call');
    }
    this.elementName = elementName;
  }

  checkValidity() {
    throw Error('Please implement checkValidity in the extended class!');
  }

  getElementParameters() {
    throw Error('Please implement getElementParameters in the extended class!');
  }
}
